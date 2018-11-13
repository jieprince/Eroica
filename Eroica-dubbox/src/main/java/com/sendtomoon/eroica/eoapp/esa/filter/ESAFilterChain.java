package com.sendtomoon.eroica.eoapp.esa.filter;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;


public interface ESAFilterChain {

	ServiceResponse doFilter(ServiceRequest request) throws Throwable;
}
