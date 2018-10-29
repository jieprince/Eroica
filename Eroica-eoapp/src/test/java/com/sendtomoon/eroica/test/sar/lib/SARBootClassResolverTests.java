package com.sendtomoon.eroica.test.sar.lib;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.sendtomoon.eroica.eoapp.sar.SARAttrs;
import com.sendtomoon.eroica.eoapp.sar.annotations.SAR;
import com.sendtomoon.eroica.eoapp.sar.lib.SARBootClassResolver;

public class SARBootClassResolverTests {
	
	protected Log logger=LogFactory.getLog(this.getClass());

	
	@Test
	public void test(){
		SARBootClassResolver resolver=new SARBootClassResolver();
		SARAttrs attrs=new SARAttrs("testSAR",new Properties());
		Class<?> bootClass=resolver.resolve(attrs,this.getClass().getClassLoader());
		SAR configure=bootClass.getAnnotation(SAR.class);
		logger.info("config="+configure);
		logger.info("basePackage="+bootClass.getPackage().getName());
	}
}
