package com.sendtomoon.eroica.eoapp.context.event;

import org.springframework.context.ConfigurableApplicationContext;

import com.sendtomoon.eroica.eoapp.context.config.EoAppConfigProperties;

public class EoAppConfigChangedEvent extends EoAppEvent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EoAppConfigProperties configProperties;

	public EoAppConfigChangedEvent(ConfigurableApplicationContext applicationContext,
			EoAppConfigProperties configProperties){
		super(applicationContext);
		this.configProperties=configProperties;
	}

	public EoAppConfigProperties getConfigProperties() {
		return configProperties;
	}
	
	
}
