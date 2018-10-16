package com.sendtomoon.eroica.eoapp.web.esa;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sendtomoon.eroica.eoapp.EoApp;
import com.sendtomoon.eroica.eoapp.web.filter.AbstractWebFilter;

public class ESAWebFilter extends AbstractWebFilter implements ApplicationContextAware {

	private ESAWebDispatcher webDispatcher;

	public ESAWebFilter() {
		this.setPattern("/esa/**");
		this.setOrder(-9999);
	}

	protected ApplicationContext applicationContext;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		try {
			webDispatcher.handleRequest(httpRequest, httpResponse);
		} catch (ServletException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		if (webDispatcher == null) {
			webDispatcher = new ESAWebDispatcher();
			webDispatcher.setDispatcher(EoApp.getInstance());
			webDispatcher.setApplicationContext(applicationContext);
			webDispatcher.afterPropertiesSet();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ESAWebDispatcher getWebDispatcher() {
		return webDispatcher;
	}

	public void setWebDispatcher(ESAWebDispatcher webDispatcher) {
		this.webDispatcher = webDispatcher;
	}

}
