package com.sendtomoon.eroica.eoapp.context.support;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.eoapp.sar.SARContext;

public interface ESAManager {

	boolean unexport(String esaName);
	
	boolean isExported(String esaName);
	
	boolean export(SARContext context,ESADefinition definition);
	
	void export(SARContext context);
	
	void unexport(SARContext context);
	
	void unexportAll();
	
	ServiceResponse handleRequest(ServiceRequest request,
			boolean includeFilters);
	
	ServiceResponse handleRequest(ServiceRequest request);
}
