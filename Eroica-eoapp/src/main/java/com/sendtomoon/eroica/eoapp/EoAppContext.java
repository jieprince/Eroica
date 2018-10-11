package com.sendtomoon.eroica.eoapp;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ConfigurableApplicationContext;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerLocal;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.context.config.EoAppConfigProperties;
import com.sendtomoon.eroica.eoapp.sar.SARContext;

public interface EoAppContext extends ApplicationControllerLocal {

	ClassLoader getClassLoader();
	
	EoAppConfigProperties getConfigProperties();
	
	String getEoAppName();
	
	
	ConfigurableApplicationContext getSpringContext();
	
	boolean isExported(String esaName) ;
	
	
	ServiceResponse handleRequest(ServiceRequest request,boolean includeFilters);
	
	void handleWebRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException;
	
	boolean startup(); 
	
	boolean startup(String sarName); 
	
	boolean shutdown();
	
	boolean shutdown(String sarName); 
	
	boolean unexport(String sarName); 
	
	boolean unexportAll(); 
	
	boolean isRunning();
	
	boolean isRunning(String sarName);
	
	boolean exists(String sarName);
	
	SARContext getSARContext(String sarName);
	
	SARContext getSARContext(String sarName,boolean requiredExists);
	
	void setServletContext(ServletContext servletContext);
	
	ServletContext getServletContext();
	 
	String[] getActiveSARs();
}
