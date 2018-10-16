package com.sendtomoon.eroica.eoapp.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.OrderComparator;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class EroicaAnnotationHandlerMapping  extends RequestMappingHandlerMapping{
	
	private boolean autoLoadInterceptors=true;

	@Override
	protected void initInterceptors() {
		if(autoLoadInterceptors) {
			List<Object> interceptors=new ArrayList<Object>();
			//
			Map<String,HandlerInterceptor> beans=getApplicationContext().getBeansOfType(HandlerInterceptor.class);
			if(beans!=null && beans.size()>0){
				interceptors.addAll(beans.values());
			}
			Map<String,WebRequestInterceptor> beans2=getApplicationContext().getBeansOfType(WebRequestInterceptor.class);
			if(beans2!=null && beans2.size()>0){
				interceptors.addAll(beans2.values());
			}
			if(interceptors.size()>0){
				 OrderComparator.sort(interceptors);
				 this.setInterceptors(interceptors.toArray());
			}
		}
		super.initInterceptors();
	}

	public boolean isAutoLoadInterceptors() {
		return autoLoadInterceptors;
	}

	public void setAutoLoadInterceptors(boolean autoLoadInterceptors) {
		this.autoLoadInterceptors = autoLoadInterceptors;
	}

	
	
}
