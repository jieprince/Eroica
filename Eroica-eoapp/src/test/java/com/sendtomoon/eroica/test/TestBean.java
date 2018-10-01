package com.sendtomoon.eroica.test;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration()
public class TestBean {

	@Bean(name = "hello")
	public String getHello() {
		return "Hello Eroica!";
	}

	public static void main(String args[]) {
		String abc = "watermaleon ,    ,abc      ";
		System.err.println(CollectionUtils.arrayToList(StringUtils.split(abc, ", ")));
	}
}
