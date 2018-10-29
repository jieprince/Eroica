package com.sendtomoon.eroica.test;

import com.sendtomoon.eroica.eoapp.sar.context.SARXmlApplicationContext;

public class SARXmlApplicationContextTests {

	public static void main(String args[]) {
		SARXmlApplicationContext context = new SARXmlApplicationContext("abc", null, null,
				TestBean.class.getPackage().getName());
		context.refresh();
		System.err.println(context.getBean(TestBean2.class).hello());
		context.close();
	}
}
