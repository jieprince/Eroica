package com.sendtomoon.eroica.eoapp.context;

import javax.servlet.ServletContext;

import org.springframework.context.Lifecycle;

import com.sendtomoon.eroica.eoapp.context.config.EoAppConfigProperties;

public interface EoAppLifecycle extends Lifecycle {

	ClassLoader getClassLoader();
	
	EoAppSpringContext  getSpringContext();
	
	
	EoAppConfigProperties getConfigProperties();
	
	void setOriginalServletContext(ServletContext servletContext);
	
	ServletContext getServletContext();
}
