package com.sendtomoon.eroica.fling.test;

import org.junit.Test;

import com.pingan.pafa.papp.test.BaseSARTest;
import com.pingan.pafa.papp.test.SARContextConfiguration;

@SARContextConfiguration(plugins = "dubbo,jetty")
public class SARTests extends BaseSARTest {

	@Test
	public void iz() throws Throwable {

		System.in.read();
	}

}
