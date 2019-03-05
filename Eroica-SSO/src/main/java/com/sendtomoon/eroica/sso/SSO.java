package com.sendtomoon.eroica.sso;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SSO {

	public static final String ATTR_KEY_UID = "WESSO_UID";

	/**
	 * <SSO主系统调用>进入登陆页时，调用此方法，创建用户身份凭证Cookie
	 * 
	 * @param request
	 * @param response
	 * @return 用户身份凭证
	 */
	String createTicket(HttpServletRequest request, HttpServletResponse response);

	/**
	 * <SSO跨域的关联系统调用>进入登陆页面时，会通知跨域的SSO子系统。跨域的关联系统则调用此方法,以在本域创建用户凭证等
	 * 
	 * @param request
	 * @param response
	 * @return 当前登陆用户的身份凭证
	 */
	String createTicket(HttpServletRequest request, HttpServletResponse response, String ticket);

	/**
	 * 获得当前登陆用户的身份凭证(ticket)
	 * 
	 * @param request
	 * @param response
	 * @return 当前登陆用户的身份凭证
	 */
	String getTicket(HttpServletRequest request, HttpServletResponse response);

	/**
	 * <SSO主系统调用>退出登陆
	 * <p>
	 * 清除缓存数据
	 * </p>
	 */
	void logout(HttpServletRequest request, HttpServletResponse response);

	/**
	 * <SSO主系统调用>登陆
	 * <p>
	 * 获得凭证Cookie{@link #getIdentityTicketSupport()}，取得Cookie值，以此为key使用UID创建缓存数据
	 * </p>
	 * 
	 * @see #getIdentityTicketSupport()
	 * @see TicketUtil
	 * @param request
	 * @param response
	 */
	void login(HttpServletRequest request, HttpServletResponse response, CacheData data);

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	Principal getUserPrincipal(HttpServletRequest request, HttpServletResponse response);

	/**
	 * 
	 * @return
	 */
	boolean isEnabled();

}
