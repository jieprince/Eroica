package com.sendtomooon.eroica.eoapp;

import com.sendtomoon.eroica.pizza.Pizza;

/**
 * 引导程序之一，启动组件之一
 * @author lbt42
 *
 */
public abstract class EoApp implements EoAppContext {

	private volatile static EoApp eoApp;

	public synchronized static EoApp getInstance() {
		if (eoApp == null) {
			eoApp = Pizza.getSpringContext().getBean(EoApp.class);
		}
		return eoApp;
	}

}
