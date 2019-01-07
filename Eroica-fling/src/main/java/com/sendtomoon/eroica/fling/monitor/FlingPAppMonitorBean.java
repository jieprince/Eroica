package com.sendtomoon.eroica.fling.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;

import com.sendtomoon.eroica.eoapp.context.event.EoAppEvent;
import com.sendtomoon.eroica.eoapp.context.event.EoAppShutdownEvent;
import com.sendtomoon.eroica.eoapp.context.event.EoAppStartupedEvent;

public class FlingPAppMonitorBean implements DisposableBean, ApplicationListener<EoAppEvent> {

	private FlingMonitorBean flingMonitor;

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onApplicationEvent(EoAppEvent event) {
		if (event instanceof EoAppStartupedEvent) {
			if (logger.isInfoEnabled()) {
				logger.info("PAppStartupedEvent {}", event);
			}
			flingMonitor.init();
		} else if (event instanceof EoAppShutdownEvent) {
			if (logger.isInfoEnabled()) {
				logger.info("PAppShutdownEvent {}", event);
			}
			flingMonitor.shutdown();
		}
	}

	@Override
	public void destroy() throws Exception {
		flingMonitor.shutdown();
	}

	public FlingMonitorBean getFlingMonitor() {
		return flingMonitor;
	}

	public void setFlingMonitor(FlingMonitorBean flingMonitor) {
		this.flingMonitor = flingMonitor;
	}

}
