package com.sendtomoon.eroica.eoapp.context;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ClassUtils;

import com.pingan.pafa.pola.Pola;
import com.sendtomoon.eroica.pizza.Pizza;
import com.sendtomoon.eroica.pizza.spring.PizzaResource;
import com.sendtomoon.eroica.pizza.spring.PizzaXmlApplicationContext;
import com.sendtomoon.eroica.eoapp.context.config.EoAppAttrs;
import com.sendtomoon.eroica.eoapp.context.config.EoAppConfigProperties;
import com.sendtomoon.eroica.eoapp.web.support.ServletContextBeanPostProcessor;
import com.sendtomoon.eroica.eoapp.web.support.ServletContextHandler;

public class EoAppSpringContextFactoryBean implements EoAppSpringContextFactory {

	protected static final String KEY_PREFIX_SPRING = ".spring.xml";

	public static final String BEAN_ID_PROPERTIES = "_eoapp_properties";

	private static final String EOAPP_DEF_SPRING_XML = "META-INF/eroica/eoapp-default.spring.xml";

	private static final String EOAPP_SPRING_XML = "META-INF/eroica/eoapp.spring.xml";

	protected Log logger = LogFactory.getLog(this.getClass());

	private EoAppConfigProperties configProperties;

	private String eoappName;

	private String webDispatcherBeanId;

	public EoAppSpringContextFactoryBean() {

	}

	public EoAppConfigProperties getConfigProperties() {
		return configProperties;
	}

	public void setConfigProperties(EoAppConfigProperties configProperties) {
		this.configProperties = configProperties;
	}

	@Override
	public EoAppSpringContext create(ClassLoader classLoader, final ServletContext originalServletContext,
			final Pola pola) {
		String eoappName = this.eoappName;
		EoAppAttrs attrs = new EoAppAttrs(eoappName, configProperties);
		ConfigurableApplicationContext applicationContext = null;
		List<Resource> resources = new ArrayList<Resource>();
		//
		resources.add(getDefResource(attrs));

		if (classLoader == null) {
			classLoader = ClassUtils.getDefaultClassLoader();
		}
		try {
			Enumeration<URL> urls = classLoader.getResources(EOAPP_SPRING_XML);
			while (urls != null && urls.hasMoreElements()) {
				URL url = urls.nextElement();
				resources.add(new UrlResource(url));
			}
		} catch (IOException e) {
			throw new FatalBeanException(e.getMessage(), e);
		}
		Set<String> plugins = attrs.getPlugins();
		if (plugins != null && plugins.size() > 0) {
			for (String plugin : plugins) {
				resources.add(new ClassPathResource("META-INF/eroica/eoapp/plugins/" + plugin + ".xml", classLoader));
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("EoApp<" + eoappName + "> plugins=" + plugins);
		}
		try {
			Enumeration<URL> urls = classLoader.getResources("META-INF/" + eoappName + ".spring.xml");
			while (urls != null && urls.hasMoreElements()) {
				URL url = urls.nextElement();
				resources.add(new UrlResource(url));
			}
		} catch (IOException e) {
			throw new FatalBeanException(e.getMessage(), e);
		}
		// -----------------------------------------------------------------------
		PizzaResource def = new PizzaResource(Pizza.GROUP_EOAPP + "/" + eoappName + KEY_PREFIX_SPRING);
		if (def.exists()) {
			resources.add(def);
		}
		// ------------------------------------------------------------------------
		Resource[] resourceArray = new Resource[resources.size()];
		resources.toArray(resourceArray);
		if (logger.isInfoEnabled()) {
			logger.info("EoApp<" + eoappName + "> spring resources=" + Arrays.toString(resourceArray));
		}
		applicationContext = new PizzaXmlApplicationContext(eoappName, resourceArray);
		final ServletContextHandler servletContextHandler = new ServletContextHandler(originalServletContext);
		applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {

			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				beanFactory.registerSingleton(BEAN_ID_PROPERTIES, configProperties);
				beanFactory.registerSingleton("_eoapp_pola", pola);
				beanFactory
						.addBeanPostProcessor(new ServletContextBeanPostProcessor(servletContextHandler, beanFactory));
			}
		});
		applicationContext.refresh();
		return new EoAppSpringContextBean(applicationContext, this.getWebDispatcherBeanId(),
				servletContextHandler.resolveServletContext(applicationContext.getBeanFactory()));
	}

	protected Resource getDefResource(EoAppAttrs attrs) {
		return new ClassPathResource(EOAPP_DEF_SPRING_XML);
	}

	public String getEoappName() {
		return eoappName;
	}

	public void setEoappName(String eoappName) {
		this.eoappName = eoappName;
	}

	public String getWebDispatcherBeanId() {
		return webDispatcherBeanId;
	}

	public void setWebDispatcherBeanId(String webDispatcherBeanId) {
		this.webDispatcherBeanId = webDispatcherBeanId;
	}

}
