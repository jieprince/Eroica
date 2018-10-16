package com.sendtomoon.eroica.eoapp.context.event;

import org.springframework.context.ConfigurableApplicationContext;

public class EoAppStartupedEvent extends EoAppEvent {

	private static final long serialVersionUID = 1L;

	public EoAppStartupedEvent(ConfigurableApplicationContext applicationContext) {
		super(applicationContext);
	}

}
