package com.alibaba.dubbo.monitor.logstat;

import java.util.Arrays;
import java.util.List;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.monitor.Monitor;

public class LogstatMonitor implements Monitor {
	
	private static final Logger statisticsLog = LoggerFactory.getLogger("dubbo.LogstatMonitor");
	
	private static URL url=URL.valueOf("logstat://");

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public void collect(URL statistics) {
		 if(statisticsLog.isInfoEnabled()){
			 statisticsLog.info(statistics.toString());
         }
	}

	@Override
	public List<URL> lookup(URL query) {
		return Arrays.asList(url);
	}
	    
	   
}
