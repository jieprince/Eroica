package com.sendtomoon.eroica.pizza.support.html;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.sendtomoon.eroica.common.utils.PURL;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;
import com.sendtomoon.eroica.pizza.spring.schema.PizzaShareBean;

public class HtmlDeployShareBean implements PizzaShareBean {

	@Override
	public BeanDefinition create(PizzaURL pizzaURL, PURL configURL) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setLazyInit(false);
		beanDefinition.setBeanClass(HtmlDeployBean.class);

		MutablePropertyValues pvs = beanDefinition.getPropertyValues();
		pvs.addPropertyValue("pizzaURL", configURL.getAbsolutePath());
		pvs.addPropertyValue("deployPath", configURL.getParameter("deployPath", ""));
		pvs.addPropertyValue("listenEnable", configURL.getParameter("listenEnable", true));
		pvs.addPropertyValue("requiredExists", configURL.getParameter("requiredExists", false));
		pvs.addPropertyValue("forceClean", configURL.getParameter("forceClean", true));
		return beanDefinition;
	}

}
