package com.sendtomoon.eroica.eoapp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ESA  {
	/**服务名*/
	String value() default "";
	/**服务名*/
	String name() default "";
	
	/**服务分组*/
	String group() default "";
	
	/**是否发布*/
	boolean export() default true;

	/**过期标志*/
	boolean deprecated() default false;

	/**提供者端并发数限制*/
	int executes() default 0;

	/**是否注册到服务注册中心*/
	boolean register() default true;

	/**权重*/
    int weight() default 0;

    int delay() default 0;

    String cluster() default "";
    
    int connections() default 0;

	int callbacks() default 0;

	/**失败重试次数*/
	int retries() default 0;

	String loadbalance() default "";

	boolean async() default false;

	/**消费者端并发限制*/
    int actives() default 0;

    /**超时限制*/
	int timeout() default 0;
	
	/**服务降级策略*/
	String mock() default "";
 
	boolean local() default false;
	
	
	boolean sent() default false;
	
	/**作用域,remote/local*/
	String scope() default "";
	
	String[] filter() default {};

	String[] protocol() default {};
	
	String[] registry() default {};
	
	String owner() default "";
	
    String layer() default "";
    
    String monitor() default "";
	
}
