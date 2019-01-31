package com.sendtomoon.eroica.fling.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;

import com.sendtomoon.eroica.eoapp.context.event.EoAppEvent;
import com.sendtomoon.eroica.eoapp.context.event.EoAppShutdownEvent;
import com.sendtomoon.eroica.eoapp.context.event.EoAppStartupedEvent;

public class FlingRootMonitorBean implements DisposableBean, ApplicationListener<EoAppEvent> {
	
	protected Logger logger=LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void onApplicationEvent(EoAppEvent event) {
		if(event instanceof EoAppStartupedEvent){
			if(logger.isInfoEnabled()) {
				logger.info("Root PAppStartupedEvent {}",event);
			}
		}else if(event instanceof EoAppShutdownEvent){
			if(logger.isInfoEnabled()) {
				logger.info("Root PAppShutdownEvent {}",event);
			}
		}
	}

	@Override
	public void destroy() throws Exception {
	}
	
}
