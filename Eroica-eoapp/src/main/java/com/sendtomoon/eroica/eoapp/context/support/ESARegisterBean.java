package com.sendtomoon.eroica.eoapp.context.support;

import org.springframework.context.ApplicationListener;

import com.sendtomoon.eroica.eoapp.sar.SARContext;
import com.sendtomoon.eroica.eoapp.sar.event.SAREvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARShutdownEvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARStartupedEvent;

public class ESARegisterBean implements ApplicationListener<SAREvent>{
	
	private ESAManager esaManager;
	
	
	@Override
	public void onApplicationEvent(SAREvent event) {
		if(event instanceof SARStartupedEvent){
			SARContext sarContext=event.getSARContext();
			esaManager.export(sarContext);
		}else if(event instanceof SARShutdownEvent){
			SARContext sarContext=event.getSARContext();
			esaManager.unexport(sarContext);
		}
	}


	public ESAManager getEsaManager() {
		return esaManager;
	}


	public void setEsaManager(ESAManager esaManager) {
		this.esaManager = esaManager;
	}
	
	
}
