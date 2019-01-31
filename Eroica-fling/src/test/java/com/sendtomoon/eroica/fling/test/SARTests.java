package com.sendtomoon.eroica.fling.test;

import org.junit.Test;

import com.sendtomoon.eroica.eoapp.test.BaseSARTest;
import com.sendtomoon.eroica.eoapp.test.SARContextConfiguration;

@SARContextConfiguration(plugins = "dubbo,jetty")
public class SARTests extends BaseSARTest {

	@Test
	public void iz() throws Throwable {

		System.in.read();
	}

}
