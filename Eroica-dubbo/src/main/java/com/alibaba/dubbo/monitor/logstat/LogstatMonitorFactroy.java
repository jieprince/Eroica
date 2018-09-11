package com.alibaba.dubbo.monitor.logstat;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;

public class LogstatMonitorFactroy implements MonitorFactory {
	
	private LogstatMonitor logstatMonitor=new LogstatMonitor();

	@Override
	public Monitor getMonitor(URL url) {
		return logstatMonitor;
	}
	
	
	    
	   
}
