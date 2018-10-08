package com.sendtomoon.eroica.eoapp.web.filter;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

 class DefaultFilterConfig implements FilterConfig{
	
	private String filterName;
	
	private Properties initParameters;
	
	private ServletContext servletContext;
	
	public DefaultFilterConfig(String filterName,Properties initParameters,ServletContext servletContext){
		this.filterName=filterName;
		if(initParameters==null){
			initParameters=new Properties();
		}
		this.initParameters=initParameters;
		this.servletContext=servletContext;
	}

	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public String getInitParameter(String name) {
		return initParameters.getProperty(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return initParameters.keys();
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}
	
	

}
