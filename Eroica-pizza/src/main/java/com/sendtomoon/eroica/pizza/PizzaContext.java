package com.sendtomoon.eroica.pizza;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;

import com.sendtomoon.eroica.pizza.classloader.PizzaURL;
import com.sendtomoon.eroica.pizza.spring.PizzaResourceListener;

public interface PizzaContext {
	
	String getAppName();
	
	/**
	 * @deprecated getDomainId
	 * */
	String getProjectId();
	
	String getDomainId();

	public  Charset getCharset();
	
	public Log getStartupLogger();
	
	public   String get(String group,String key);
	
	public   String get(String path);
	
	boolean exists(String group,String key);
	
	boolean exists(String path);
	
	public  PizzaManager getDefaultManager();
	
	public PizzaManager createManager(String configURL);
	
	boolean  registerListener(PizzaResourceListener listener);
	
	boolean  unregisterListener(PizzaURL pizzaURL);
	
	
}
