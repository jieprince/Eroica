package com.sendtomoon.eroica.eoapp.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.OrderComparator;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

public class EroicaAnnotationMethodHandlerAdapter extends RequestMappingHandlerAdapter  implements InitializingBean {

	
	@Override
	public void afterPropertiesSet()  {
		Map<String,HandlerMethodArgumentResolver> beans=this.getApplicationContext().getBeansOfType(HandlerMethodArgumentResolver.class);
		if(beans!=null && beans.size()>0){
			HandlerMethodArgumentResolver temp[]=new HandlerMethodArgumentResolver[beans.size()];
			 beans.values().toArray(temp);
			 OrderComparator.sort(temp);
			 this.setCustomArgumentResolvers(Arrays.asList(temp));
		}
		Map<String,HandlerMethodReturnValueHandler> returnValueHandlers=this.getApplicationContext().getBeansOfType(HandlerMethodReturnValueHandler.class);
		if(returnValueHandlers!=null &&returnValueHandlers.size()>0){
			HandlerMethodReturnValueHandler temp[]=new HandlerMethodReturnValueHandler[returnValueHandlers.size()];
			returnValueHandlers.values().toArray(temp);
			 OrderComparator.sort(temp);
			 this.setReturnValueHandlers(Arrays.asList(temp));
		}
		
		Map<String,HttpMessageConverter> messageConverterMap=this.getApplicationContext().getBeansOfType(HttpMessageConverter.class);
		if(messageConverterMap!=null &&messageConverterMap.size()>0){
			StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
			stringHttpMessageConverter.setWriteAcceptCharset(false);
			List<HttpMessageConverter<?>> messageConverterList = new ArrayList<HttpMessageConverter<?>>(8);
			//
			messageConverterList.add(new ByteArrayHttpMessageConverter());
			messageConverterList.add(stringHttpMessageConverter);
			messageConverterList.add(new SourceHttpMessageConverter());
			//
			for(HttpMessageConverter mc:messageConverterMap.values()){
				messageConverterList.add(mc);
			}
			messageConverterList.add(new AllEncompassingFormHttpMessageConverter());
			this.setMessageConverters(messageConverterList);
		}
		super.afterPropertiesSet();
	}

	
}
