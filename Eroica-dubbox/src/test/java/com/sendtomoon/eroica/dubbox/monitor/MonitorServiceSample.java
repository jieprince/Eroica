package com.sendtomoon.eroica.dubbox.monitor;

import java.util.List;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.monitor.MonitorService;

public class MonitorServiceSample implements MonitorService {

	@Override
	public void collect(URL statistics) {
		System.err.println("statistics="+statistics);
	}

	@Override
	public List<URL> lookup(URL query) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
