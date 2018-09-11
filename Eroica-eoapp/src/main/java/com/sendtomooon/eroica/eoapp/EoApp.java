package com.sendtomooon.eroica.eoapp;

import com.pingan.pafa.pizza.Pizza;

public abstract class EoApp implements EoAppContext {

	private volatile static EoApp papp;

	public synchronized static EoApp getInstance() {
		if (papp == null) {
			papp = Pizza.getSpringContext().getBean(EoApp.class);
		}
		return papp;
	}

}
