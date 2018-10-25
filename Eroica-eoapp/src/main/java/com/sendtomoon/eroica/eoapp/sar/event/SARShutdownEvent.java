package com.sendtomoon.eroica.eoapp.sar.event;

import com.sendtomoon.eroica.eoapp.sar.SARContext;

public class SARShutdownEvent extends SAREvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SARShutdownEvent(SARContext sarContext){
		super(sarContext);
	}

}
