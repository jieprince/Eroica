package com.sendtomoon.eroica.eoapp.esa.exception;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;

public interface ESAExceptionResolver {

	ServiceResponse resolveException(
			ServiceRequest request, Throwable ex); 
}
