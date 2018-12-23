package com.sendtomoon.eroica.eoapp.esa.annotation;

import java.lang.annotation.Annotation;

import org.springframework.core.MethodParameter;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;

public interface AnnotationArgumentResolver {

	Class<? extends Annotation> getAnnotationClass();
	
	Object resolveArgument(MethodParameter methodParameter,Annotation annotation, ServiceRequest request) throws Exception;

}
