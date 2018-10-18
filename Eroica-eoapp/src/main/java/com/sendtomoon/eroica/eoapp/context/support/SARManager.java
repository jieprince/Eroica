package com.sendtomoon.eroica.eoapp.context.support;

import java.util.List;
import java.util.Set;

import com.sendtomoon.eroica.eoapp.sar.SARContext;

public interface SARManager {

	SARContext getSARContext(String sarName);
	
	SARContext getSARContext(String sarName,boolean requiredExists);
	
	void startupSARs(Set<String> sarNames);
	
	void startupSARs(String sarList);
	
	void refreshSARs(String sarList);
	
	boolean exists(String sarName);
	
	List<SARContext> listSARContext();
	
	void shutdown();
	 
	 boolean shutdown(String sarName);
	 
	 boolean isRunning(String sarName);
	 
	 boolean startup(String sarName);
	 
}
