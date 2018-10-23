package com.sendtomoon.eroica.eoapp.sar.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import com.sendtomoon.eroica.eoapp.sar.SARContext;

public class SAREvent  extends ApplicationEvent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SAREvent(SARContext sarContext){
		super(sarContext);
	}
	
	public SARContext getSARContext(){
		return (SARContext)this.getSource();
	}
	
	public ConfigurableApplicationContext getSARSpringContext(){
		return getSARContext().getSpringContext();
	}
	
	public String getSARName(){
		return getSARContext().getSARName();
	}
}
