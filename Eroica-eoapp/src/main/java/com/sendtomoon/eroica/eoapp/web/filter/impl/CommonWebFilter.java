package com.sendtomoon.eroica.eoapp.web.filter.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;

import com.sendtomoon.eroica.common.exception.EroicaException;
import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomoon.eroica.common.web.util.ContentTypeUtil;
import com.sendtomoon.eroica.eoapp.web.filter.AbstractWebFilter;
import com.sendtomoon.eroica.eoapp.web.filter.WebFilter;

public class CommonWebFilter extends AbstractWebFilter implements WebFilter, Ordered {

	protected Log logger = LogFactory.getLog(this.getClass());

	private ContentTypeUtil contentTypeUtil = new ContentTypeUtil();

	private static final String HEADER_PRAGMA = "Pragma";

	private static final String HEADER_EXPIRES = "Expires";

	private static final String HEADER_CACHE_CONTROL = "Cache-Control";

	private String defaultCharset;

	private boolean preventCaching = true;

	private boolean enableResolveCharset = true;

	private boolean logEnable = true;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// ----------------------------------------------
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		// ----------------
		String requestId = resolveRequestId(httpRequest, httpResponse);
		MDCUtil.set(requestId);
		// ---------------------------------
		this.resolveCharset(httpRequest, httpResponse);
		this.preventCaching(httpResponse);
		long t1 = -1l;
		if ((this.isLogEnable()) && logger.isInfoEnabled()) {
			t1 = System.nanoTime();
			logger.info("Begin Request[" + getRequestUri(httpRequest, true) + "]...");
		}
		try {
			chain.doFilter(request, response);
		} finally {
			if ((this.isLogEnable()) && logger.isInfoEnabled()) {
				logger.info("[T=" + requestId + "]>>>>>>> Completed request[" + getRequestUri(httpRequest, false) + "]["
						+ (System.nanoTime() - t1) / 1000 / 1000.0 + "ms].");
			}
			MDCUtil.clear();
		}
	}

	private static final String REQUESTID_PATTERN_STRING = "^[\\w\\-]{16,}$";

	/***
	 * requestId http header name
	 */
	public static final String REQUEST_ID_HEADER_NAME = "X-REQ-ID";

	private static final Pattern REQUEST_PATTERN = Pattern.compile(REQUESTID_PATTERN_STRING);

	private String resolveRequestId(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		String requestId = httpRequest.getHeader(REQUEST_ID_HEADER_NAME);
		if (requestId != null && (requestId = requestId.trim()).length() > 0) {
			check(requestId);
		} else {
			requestId = MDCUtil.generateRequestId();
			httpResponse.addHeader("Access-Control-Expose-Headers", REQUEST_ID_HEADER_NAME);
			httpResponse.setHeader(REQUEST_ID_HEADER_NAME, requestId);
		}
		return requestId;
	}

	private static void check(String requestId) {
		if (requestId == null) {
			throw new java.lang.IllegalArgumentException("ESA Name is null.");
		}
		if (!REQUEST_PATTERN.matcher(requestId).matches()) {
			throw new IllegalArgumentException(
					"RequestId=" + requestId + ",Not matched by regex=" + REQUESTID_PATTERN_STRING + ".");
		}
	}

	protected String getRequestUri(HttpServletRequest request, boolean includeQueryString) {
		String path = request.getRequestURI();
		String qs = "";
		if (includeQueryString) {
			qs = request.getQueryString();
			if (qs != null && qs.length() > 0) {
				qs = "?" + qs;
			} else {
				qs = "";
			}
		}
		return path + qs;
	}

	public CommonWebFilter() {
		this.setPattern("/**");
		this.setOrder(-999999);
	}

	public MediaType getMediaType(ServletRequest request) {
		return contentTypeUtil.getMediaType(request);
	}

	public String resolveCharset(HttpServletRequest request, HttpServletResponse response) {
		String requestCharset = null;
		if (enableResolveCharset) {
			requestCharset = contentTypeUtil.getCharset(request, defaultCharset);
		} else {
			requestCharset = defaultCharset;
		}
		// ----------------------------------------------------------
		if (requestCharset != null) {
			try {
				request.setCharacterEncoding(requestCharset);
				response.setCharacterEncoding(requestCharset);
			} catch (UnsupportedEncodingException e) {
				throw new EroicaException(e.getMessage(), e);
			}
		}
		return requestCharset;
	}

	public void preventCaching(HttpServletResponse response) {
		if (preventCaching) {
			response.setHeader(HEADER_PRAGMA, "no-cache");
			response.setDateHeader(HEADER_EXPIRES, 1L);
			response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
			response.addHeader(HEADER_CACHE_CONTROL, "no-store");
		}
	}

	public boolean isLogEnable() {
		return logEnable;
	}

	public void setLogEnable(boolean logEnable) {
		this.logEnable = logEnable;
	}

	public String getDefaultCharset() {
		return defaultCharset;
	}

	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	public boolean isPreventCaching() {
		return preventCaching;
	}

	public void setPreventCaching(boolean preventCaching) {
		this.preventCaching = preventCaching;
	}

	public boolean isEnableResolveCharset() {
		return enableResolveCharset;
	}

	public void setEnableResolveCharset(boolean enableResolveCharset) {
		this.enableResolveCharset = enableResolveCharset;
	}

}
