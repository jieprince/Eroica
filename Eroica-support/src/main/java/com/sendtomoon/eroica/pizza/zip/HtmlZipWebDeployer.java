package com.sendtomoon.eroica.pizza.zip;

import com.sendtomoon.eroica.pizza.Pizza;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;

public class HtmlZipWebDeployer extends com.sendtomoon.eroica.pizza.support.html.HtmlDeployBean {

	public void setConfigGroup(String group) {
	}

	public void setConfigKey(String key) {
		this.setPizzaURL(PizzaURL.valueOf(Pizza.GROUP_ZIP + "/" + key));
	}

}
