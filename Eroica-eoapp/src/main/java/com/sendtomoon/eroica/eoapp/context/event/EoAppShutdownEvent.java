package com.sendtomoon.eroica.eoapp.context.event;

import org.springframework.context.ConfigurableApplicationContext;

public class EoAppShutdownEvent extends EoAppEvent{

	private static final long serialVersionUID = 1L;

	public EoAppShutdownEvent(ConfigurableApplicationContext applicationContext){
		super(applicationContext);
	}
	
	

}
