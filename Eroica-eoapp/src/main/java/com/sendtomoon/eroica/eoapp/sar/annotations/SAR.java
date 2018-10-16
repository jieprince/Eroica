package com.sendtomoon.eroica.eoapp.sar.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SAR {
	
	boolean webEnable() default false;
	
	String[] webPatterns() default {};
	
	int order() default 0;
	
	String defCharset() default "";
	
	String[] plugins() default {};
	
}
