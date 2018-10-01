package com.sendtomoon.eroica.eoapp.sar;

import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ConfigurableApplicationContext;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerLocal;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;

public interface SARContext  extends ApplicationControllerLocal{

	 ConfigurableApplicationContext getSpringContext();
	 
	 ServletContext getServletContext();
	 
	 String getSARName(); 
	 
	 Collection<ESADefinition> getESADefinitions();
	 
	 boolean handleWebRequest(HttpServletRequest request,HttpServletResponse response) 
			 throws ServletException;
	 
	 
	 boolean isRunning();
	 
	 void shutdown();
	 
	 boolean startup();
	 
}
