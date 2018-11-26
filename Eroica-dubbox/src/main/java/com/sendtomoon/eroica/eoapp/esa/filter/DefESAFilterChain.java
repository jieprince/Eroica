package com.sendtomoon.eroica.eoapp.esa.filter;

import java.util.Arrays;
import java.util.List;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationController;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.exception.EroicaException;

public final class DefESAFilterChain implements ESAFilterChain{
	
	private volatile List<ESAFilter> filters;
	
	
	private volatile ApplicationController dispatcher;
	
	
	private volatile int curIdx;
	
	
	
	public  DefESAFilterChain(ApplicationController dispatcher,List<ESAFilter> filters){
		this.filters=filters;
		this.dispatcher=dispatcher;
	}
	
	public DefESAFilterChain(ApplicationController dispatcher,ESAFilter... filters){
		this.filters=Arrays.asList(filters);
		this.dispatcher=dispatcher;
	}
	

	@Override
	public final ServiceResponse doFilter(ServiceRequest request) throws Throwable {
		while(curIdx!=filters.size()){
			
			ESAFilter filter=filters.get(curIdx);
			curIdx++;
			//--------------
			if(filter.match(request)){
				return filter.doFilter(request, this);
			}
		}
		return doFinal(request,this.dispatcher);
		
	}

	protected final ServiceResponse doFinal(ServiceRequest request,ApplicationController dispatcher) throws Throwable{
		if(dispatcher==null){
			throw new EroicaException("ESA<"+request.getRequestedServiceID()+">not found dispatcher.");
		}
		return dispatcher.handleRequest(request);
	}



	
	
	
	
	
	
}
