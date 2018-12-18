package com.sendtomoon.eroica.mongodb;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.sendtomoon.eroica.common.utils.PURL;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;
import com.sendtomoon.eroica.pizza.spring.PizzaResource;
import com.sendtomoon.eroica.pizza.spring.schema.PizzaShareBean;

public class MongoTemplateShareBean implements PizzaShareBean {

	@Override
	public BeanDefinition create(PizzaURL pizzaURL, PURL configURL) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setLazyInit(configURL.getParameter("lazy", false));
		beanDefinition.setBeanClass(MongoTemplateFactoryBean.class);
		MutablePropertyValues pvs = beanDefinition.getPropertyValues();
		pvs.addPropertyValue("configureResource", new PizzaResource(pizzaURL));
		pvs.addPropertyValue("initializeOnStartup", configURL.getParameter("initializeOnStartup", true));
		return beanDefinition;
	}

}
