package com.sendtomoon.eroica.fling.test;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class TestSarStartupFailed implements InitializingBean{

	@Override
	public void afterPropertiesSet() throws Exception {
		/*if(1!=2){
			throw new RuntimeException("aaa");
		}*/
	}

}
