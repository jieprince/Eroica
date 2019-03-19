package com.sendtomoon.eroica.pizza.impl;

import com.sendtomoon.eroica.common.utils.PURL;
import com.sendtomoon.eroica.pizza.PizzaManager;
import com.sendtomoon.eroica.pizza.PizzaManagerFactory;

public class ClassPathPizzaManagerFactory implements PizzaManagerFactory {

	@Override
	public PizzaManager create(PURL configURL) {
		ClassPathPizzaManager manager = new ClassPathPizzaManager();
		configURL = configURL.addParameter("localBackup", false);
		if (configURL.getParameter("rootPath") == null) {
			configURL = configURL.addParameter("rootPath", "/");
		}
		manager.init(configURL);
		return manager;
	}

}
