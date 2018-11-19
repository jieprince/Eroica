package com.sendtomoon.eroica.eoapp.esa.annotation;

import java.lang.annotation.Annotation;

public abstract class AbstractAnnotationArgumentResolver implements AnnotationArgumentResolver{
	
	private Class<? extends Annotation> annotationClass;

	@Override
	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	

	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

}
