package com.sendtomoon.eroica.eoapp.esa.filter;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;

public interface ESAFilter  {

	boolean match(ServiceRequest request) throws Throwable;
	
	ServiceResponse doFilter(ServiceRequest request,ESAFilterChain chain)throws Throwable;
	
}
