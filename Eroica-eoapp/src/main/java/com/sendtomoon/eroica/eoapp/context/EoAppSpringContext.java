package com.sendtomoon.eroica.eoapp.context;

import javax.servlet.ServletContext;

import org.springframework.context.ConfigurableApplicationContext;

import com.sendtomoon.eroica.eoapp.context.support.ESAManager;
import com.sendtomoon.eroica.eoapp.context.support.SARManager;
import com.sendtomoon.eroica.eoapp.web.EoAppWebDispatcher;

public interface EoAppSpringContext {

	ConfigurableApplicationContext getApplicationContext();
	
	EoAppWebDispatcher getWebDispatcher();

	void close();
	 
	SARManager getSarManager() ;

	ESAManager getEsaManager();
	
	ServletContext getServletContext();
}
