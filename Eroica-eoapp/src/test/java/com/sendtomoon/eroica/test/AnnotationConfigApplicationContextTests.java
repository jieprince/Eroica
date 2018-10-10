package com.sendtomoon.eroica.test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AnnotationConfigApplicationContextTests {

	public static void main(String args[]) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestBean.class);
		System.err.println(context.getBean("hello"));
		System.err.println(context.getBean(TestBean2.class).hello());
		context.close();

	}
}
