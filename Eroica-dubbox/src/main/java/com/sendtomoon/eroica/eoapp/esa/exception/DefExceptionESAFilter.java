package com.sendtomoon.eroica.eoapp.esa.exception;

import org.springframework.core.Ordered;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.esa.filter.AbstractESAFilter;
import com.sendtomoon.eroica.eoapp.esa.filter.ESAFilterChain;

public class DefExceptionESAFilter extends AbstractESAFilter{
	
	private ESAExceptionResolver exceptionResolver;
	
	public DefExceptionESAFilter(){
		this.setOrder(Ordered.LOWEST_PRECEDENCE);
	}

	@Override
	public ServiceResponse doFilter(ServiceRequest request, ESAFilterChain chain)
			throws Throwable {
		try{
			return chain.doFilter(request);
		}catch(Throwable th){
			return getExceptionResolver().resolveException(request, th);
		}
	}

	public ESAExceptionResolver getExceptionResolver() {
		return exceptionResolver;
	}

	public void setExceptionResolver(ESAExceptionResolver exceptionResolver) {
		this.exceptionResolver = exceptionResolver;
	}
	
	


}
	
