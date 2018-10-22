package com.sendtomoon.eroica.eoapp.protocol.servlet;

import javax.servlet.ServletContext;

import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ServletContextAware;

public class PafaServletContext implements ServletContextAware{
	private ServletContext context;
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.context = servletContext;
	}

	@Bean
	public ServletContext getContext() {
		return context;
	}

	public void setContext(ServletContext context) {
		this.context = context;
	}

}
