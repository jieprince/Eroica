package com.sendtomoon.eroica.eoapp.web.filter;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

 class DefaultServletConfig implements ServletConfig {

	private String servletName;
	
	private Properties initParameters;
	
	private ServletContext servletContext;
	
	public DefaultServletConfig(String servletName,Properties initParameters,ServletContext servletContext){
		this.servletName=servletName;
		if(initParameters==null){
			initParameters=new Properties();
		}
		this.initParameters=initParameters;
		this.servletContext=servletContext;
	}

	

	@Override
	public String getServletName() {
		return servletName;
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
