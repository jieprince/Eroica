package com.sendtomoon.eroica.fling.monitor;

import org.springframework.context.ApplicationListener;

import com.sendtomoon.eroica.eoapp.sar.event.SAREvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARShutdownEvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARStartupFailedEvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARStartupedEvent;

public class FlingSARMonitorBean implements ApplicationListener<SAREvent> {

	private FlingMonitor flingMonitor;
	
	@Override
	public void onApplicationEvent(SAREvent event) {
		if(!flingMonitor.isEnable())
		{
			return;
		}	
		if(event instanceof SARStartupedEvent){
			flingMonitor.reportForSARStartuped(event.getSARName());
		}else if(event instanceof SARShutdownEvent){
			flingMonitor.reportForSARStoped(event.getSARName());
		}else if(event instanceof SARStartupFailedEvent){
			flingMonitor.reportForSARStartupFailed(event.getSARName());
		}
			

	}

	public FlingMonitor getFlingMonitor() {
		return flingMonitor;
	}

	public void setFlingMonitor(FlingMonitor flingMonitor) {
		this.flingMonitor = flingMonitor;
	}

	

	/*@Override
	public ModelAndView resolveException(
			HttpServletRequest httpServletRequest,
			HttpServletResponse HttpServletResponse, Object handler,
			Exception exception) {
		if(flingMonitor!=null && isReportError()){
			String msg="Handle request:"+httpServletRequest.getRequestURI()+" by handler:"
					+(handler==null?"Unkown":handler.getClass().getName())+" error,cause:"+exception.getMessage();
			flingMonitor.reportError(msg,exception, sarName);
		}
		return null;
	}*/
	
	
/*
	@Override
	public ServiceResponse resolveException(ServiceRequest request, Throwable ex) {
		if(flingMonitor!=null && this.isReportError()){
			String msg="Handle esa request:"+request.getRequestedServiceID()+" error,cause:"+ex.getMessage();
			flingMonitor.reportError(msg,ex, sarName);
		}
		return null;
	}
*/
	/*
	@Override
	public void stop(Runnable paramRunnable) {
		isRunning=false;
		if(flingMonitor!=null){
			flingMonitor.reportForSARShutdown(sarName);
		}
		if(paramRunnable!=null){
			paramRunnable.run();
		}
	}*/
	
	

	
	

	
	

}
