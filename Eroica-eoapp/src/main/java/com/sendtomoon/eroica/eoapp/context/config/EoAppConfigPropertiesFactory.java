package com.sendtomoon.eroica.eoapp.context.config;

import java.io.InputStream;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

import com.sendtomoon.eroica.pizza.Pizza;
import com.sendtomoon.eroica.pizza.PizzaConstants;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;

public class EoAppConfigPropertiesFactory
		implements ApplicationContextAware, InitializingBean, FactoryBean<EoAppConfigProperties> {

	protected Log logger = LogFactory.getLog(this.getClass());

	private static final String KEY_PREFIX_CONFIG = ".properties";

	private static final String CONGIGLOCATION_NAME = "classpath*:/META-INF/eroica/eoapp-default.properties";

	private ConfigurableApplicationContext applicationContext;

	private EoAppConfigProperties properties;

	@Override
	public EoAppConfigProperties getObject() throws Exception {
		return properties;
	}

	@Override
	public Class<?> getObjectType() {
		return EoAppConfigProperties.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Resource[] resources = applicationContext.getResources(CONGIGLOCATION_NAME);
		String appName = Pizza.getAppName();
		PizzaURL pizzaURL = PizzaURL.valueOf(PizzaConstants.GROUP_EOAPP + "/" + appName + KEY_PREFIX_CONFIG);
		final EoAppConfigProperties properties = new EoAppConfigProperties(appName, pizzaURL);
		if (resources != null) {
			for (Resource resource : resources) {
				if (logger.isInfoEnabled()) {
					logger.info("ReadEoappConfigs:" + resource);
				}
				InputStream input = null;
				try {
					input = resource.getInputStream();
					properties.load(input);
				} finally {
					if (input != null)
						input.close();
				}
			}
		}
		// ----------------------------------------------------------------------------------------
		properties.refresh();
		properties.load(pizzaURL, false);
		// --------------------------------------------------
		PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}", ":", false);
		PlaceholderResolver placeholderResolver = new PlaceholderResolver() {
			@Override
			public String resolvePlaceholder(String key) {
				String value = properties.getProperty(key);
				if (value == null) {
					value = System.getProperty(key);
				}
				return value;
			}
		};
		Set<String> keys = properties.stringPropertyNames();
		for (String key : keys) {
			String value = properties.getProperty(key);
			if (value != null && value.length() > 5) {
				String newValue = helper.replacePlaceholders(value, placeholderResolver);
				if (!newValue.equals(value)) {
					properties.put(key, newValue);
				}
			}
		}

		this.properties = properties;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) context;
	}

}
