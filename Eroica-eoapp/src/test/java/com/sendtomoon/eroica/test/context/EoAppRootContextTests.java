package com.sendtomoon.eroica.test.context;

import org.junit.Test;

import com.sendtomoon.eroica.eoapp.context.EoAppRootContext;
import com.sendtomoon.eroica.eoapp.context.config.EoAppConfigProperties;

public class EoAppRootContextTests {

	@Test
	public void test() throws Exception {
		try {
			new EoAppRootContext().getBean(EoAppConfigProperties.class);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
