package com.sendtomooon.eroica.eoapp.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class EoAppEvent  extends ApplicationEvent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EoAppEvent(ConfigurableApplicationContext applicationContext){
		super(applicationContext);
	}
	
	public ConfigurableApplicationContext getApplicationContext(){
		return (ConfigurableApplicationContext)this.getSource();
	}
	
}
