package com.sendtomoon.eroica.sso;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSON;
import com.sendtomoon.eroica.pizza.Pizza;
import com.sendtomoon.eroica.redis.Redis;
import com.sendtomoon.eroica.redis.cache.RedisCache;
import com.sendtomoon.eroica.sso.ticket.TicketUtil;

public class SSOBean implements SSO, InitializingBean {

	/**
	 * 缓存HTTPSession使用的Key
	 */
	private static final String PRINCIAL_ATTR_KEY = "_eroica_pizza_sso_user_princial";

	private Object NOT_LOGIN_FLAG = new Object();

	protected Log log = LogFactory.getLog("SSO");

	private boolean destoryTicketOnCacheExpired = true;

	/***
	 * 默认的过期时间，30分钟
	 * 
	 * @see #expiresTime
	 */
	private static final int DEFAULT_CACHE_EXPIRES_TIME = 30 * 60;

	/***
	 * 缓存数据过期时间，单位秒
	 */
	private int cacheExpiresTime = DEFAULT_CACHE_EXPIRES_TIME;

	private RedisCache<String> shareCache;

	private Redis redis;

	/***
	 * 用户凭证支持类
	 */
	private TicketUtil ticketUtil;

	private boolean enabled;

	@Override
	public String createTicket(HttpServletRequest request, HttpServletResponse response) {
		return this.ticketUtil.create(request, response);
	}

	@Override
	public String getTicket(HttpServletRequest request, HttpServletResponse response) {
		return this.ticketUtil.get(request, response, false);
	}

	@Override
	public String createTicket(HttpServletRequest request, HttpServletResponse response, String ticket) {
		return this.ticketUtil.create(request, response, ticket);
	}

	@Override
	public void login(HttpServletRequest request, HttpServletResponse response, CacheData data) {
		String uid = data.getUid();
		// 取得用户凭证
		String ticket = this.ticketUtil.get(request, response, true);
		if (log.isDebugEnabled()) {
			log.debug("User[" + uid + "]logining,ticket<" + ticket + ">.");
		}

		// 取登陆IP
		String ip = HttpClientIpUtils.getIp(request, log);
		data.setIp(ip);
		data.setExpiresTime(this.cacheExpiresTime);
		long cur = System.currentTimeMillis();
		data.setLoginTime(cur);
		// 设置最后更新者的信息
		data.setLastAccTime(cur);
		data.setLastAppName(Pizza.getAppName());
		// 保存到缓存
		if (log.isDebugEnabled()) {
			log.debug("Save cache data<" + data + "> by ticket<" + ticket + ">,cacheExpiresTime=" + cacheExpiresTime
					+ " sec.");
		}
		shareCache.set(ticket, JSON.toJSONString(data.peek()), cacheExpiresTime);
		if (log.isInfoEnabled()) {
			log.info("User[" + uid + "]logined success.");
		}
		request.setAttribute(PRINCIAL_ATTR_KEY, new UserPrincipal(ticket, data));

	}

	@Override
	public UserPrincipal getUserPrincipal(HttpServletRequest request, HttpServletResponse response) {
		Object up = request.getAttribute(PRINCIAL_ATTR_KEY);
		if (up != null) {
			if (up == NOT_LOGIN_FLAG) {
				return null;
			} else {
				return (UserPrincipal) up;
			}
		}
		// 取得用户凭证
		try {
			String ticketByCookie = this.getTicket(request, response);
			if (ticketByCookie == null) {
				if (log.isDebugEnabled()) {
					log.debug("Get User Principal failure,not found sso cookie in request");
				}
				// 表示未登陆
				return null;
			}
			UserPrincipal temp = getUserPrincipal(request, response, ticketByCookie);
			if (temp != null) {
				request.setAttribute(PRINCIAL_ATTR_KEY, temp);
			} else {
				request.setAttribute(PRINCIAL_ATTR_KEY, NOT_LOGIN_FLAG);
			}

			return temp;
		} catch (SSOException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new SSOException("getUserPrincipal error:" + ex.getMessage(), ex);
		}
	}

	protected UserPrincipal getUserPrincipal(HttpServletRequest request, HttpServletResponse response,
			String ticketByCookie) {
		UserPrincipal principal = null;
		// 从远程缓存服务器获取
		CacheData data = getDataByCache(request, response, ticketByCookie);
		if (data != null) {
			principal = new UserPrincipal(ticketByCookie, data);
		} else {
			if (destoryTicketOnCacheExpired) {
				// 删除Cookie
				this.getTicketUtil().destory(request, response);
			}

			return null;
		}
		return principal;
	}

	@SuppressWarnings("unchecked")
	protected CacheData getDataByCache(HttpServletRequest request, HttpServletResponse response, String ticket) {
		CacheData data = null;
		// 获取SSO数据
		String datas = (String) shareCache.get(ticket);
		if (datas != null) {
			data = new CacheData(JSON.parseObject(datas, Map.class));
		}

		if (data == null || data.getUid() == null) {
			log.warn("Not found cache for Ticket<" + ticket + ">.");
		} else {
			int expiresTime = data.getExpiresTime();
			if (expiresTime <= 0) {
				expiresTime = this.cacheExpiresTime;
			}
			if (log.isDebugEnabled()) {
				log.debug("Get cache by Ticket<" + ticket + ">,cacheExpiresTime<" + expiresTime + " sec>,cache data="
						+ data);
			}
			// 设置最后更新者的信息
			data.setLastAccTime(System.currentTimeMillis());
			data.setLastAppName(Pizza.getAppName());
			// 异步刷新缓存
			shareCache.set(ticket, JSON.toJSONString(data.peek()), expiresTime);

		}
		return data;
	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		String ticket = this.getTicket(request, response);
		if (ticket != null) {
			logout(request, response, ticket, false);
		}
	}

	public void logout(HttpServletRequest request, HttpServletResponse response, String ticket, boolean isLocalLogout) {
		if (log.isDebugEnabled()) {
			log.debug("logout by ticket<" + ticket + ">..");
		}
		// 删除Cookie
		this.ticketUtil.destory(request, response);
		request.setAttribute(PRINCIAL_ATTR_KEY, NOT_LOGIN_FLAG);
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(PRINCIAL_ATTR_KEY);
		}

		if (!isLocalLogout) {
			if (log.isDebugEnabled()) {
				log.debug("destory cache by ticket<" + ticket + ">..");
			}
			this.shareCache.remove(ticket);
		}
	}

	public int getCacheExpiresTime() {
		return cacheExpiresTime;
	}

	public void setCacheExpiresTime(int cacheExpiresTime) {
		this.cacheExpiresTime = cacheExpiresTime;
	}

	public synchronized void afterPropertiesSet() throws Exception {
		if (this.ticketUtil == null) {
			throw new java.lang.IllegalArgumentException("Not setter ticketUtil.");
		}
		if (log.isInfoEnabled()) {
			log.info("CacheExpiresTime=" + this.cacheExpiresTime + "s" + ",destoryTicketOnCacheExpired="
					+ this.destoryTicketOnCacheExpired);
		}
		this.shareCache = redis.loadCache("$eroica_admin_sso", String.class);
		SSOContext.SSO = this;
	}

	public TicketUtil getTicketUtil() {
		return ticketUtil;
	}

	public void setTicketUtil(TicketUtil ticketUtil) {
		this.ticketUtil = ticketUtil;
	}

	public boolean isDestoryTicketOnCacheExpired() {
		return destoryTicketOnCacheExpired;
	}

	public void setDestoryTicketOnCacheExpired(boolean destoryTicketOnCacheExpired) {
		this.destoryTicketOnCacheExpired = destoryTicketOnCacheExpired;
	}

	public Redis getRedis() {
		return redis;
	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
