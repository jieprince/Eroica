package com.sendtomoon.eroica.eoapp.protocol.jetty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ResourceUtils;

import com.sendtomoon.eroica.common.utils.EroicaMeta;

public class JettyResourceFilter implements Filter {

	private final long start = System.currentTimeMillis();

	private JettyAttrs config;

	public void init(FilterConfig filterConfig) throws ServletException {
		config = (JettyAttrs) filterConfig.getServletContext().getAttribute(
				JettyAttrs.ATTR_KEY);
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if (response.isCommitted()) {
			return;
		}
		if (!handleRequest(request, response)) {
			chain.doFilter(request, response);
		}
	}

	private boolean handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		String uri = request.getRequestURI();
		if (config.getWebroot() == null) {
			if (uri.endsWith("/favicon.ico")) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return true;
			}
			return false;
		}
		String context = request.getContextPath();
		if (uri.endsWith("/favicon.ico")) {
			//
		} else if (context != null && context.length() > 0
				&& !"/".equals(context)) {
			uri = uri.substring(context.length());
		}
		if (uri.length() == 0 || uri.equals("/")) {
			if (config.getHomepage() != null) {
				uri = config.getHomepage();
			} else {
				uri = "/index.html";
			}
		}
		if (uri.charAt(0) != '/') {
			uri = "/" + uri;
		}
		if(uri.startsWith("/WEB-INF")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return true;
		}
		URL url = resolveURL(uri);
		if (url == null) {
			return handleRequestForNotFound(request, response, uri, url);
		}
		long lastModified = -1;
		long since = request.getDateHeader("If-Modified-Since");
		if (since > 0 && ResourceUtils.isFileURL(url)) {
			lastModified = getLastModified(url);
			if (since >= lastModified) {
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return true;
			}
		}//
		InputStream input = null;
		try {
			input = url.openStream();
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		if (input == null) {
			return handleRequestForNotFound(request, response, uri, url);
		}
		try {
			OutputStream output = response.getOutputStream();
			byte[] buffer = new byte[8192];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
			output.flush();
		} finally {
			input.close();
		}
		if (lastModified > 0) {
			response.setDateHeader("Last-Modified", lastModified);
		}
		return true;
	}

	private boolean handleRequestForNotFound(HttpServletRequest request,
			HttpServletResponse response, String uri, URL fileURL)
			throws IOException {
		// --------------------------------------------------------
		if (uri.endsWith("/favicon.ico")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return true;
		} else if (uri.equals("/index.html")) {
			OutputStream output = response.getOutputStream();
			output.write(("<!DOCTYPE HTML><html><head><meta charset=\"utf-8\" /><title>欢迎使用Pafa5！</title></head>"
					+ "<body><h1>欢迎使用Pafa" + EroicaMeta.VERSION + "！</h1> by Jetty8.</body></html>")
					.getBytes("UTF-8"));
			output.flush();
			return true;
		}
		return false;
	}

	private URL resolveURL(String uri) {
		try {
			return ResourceUtils.getURL(config.getWebroot() + uri);
		} catch (Exception e) {
			return null;
		}
	}

	private long getLastModified(URL url) {
		File file = new File(url.getFile());
		if (file.exists()) {
			return file.lastModified();
		}
		return start;
	}

}