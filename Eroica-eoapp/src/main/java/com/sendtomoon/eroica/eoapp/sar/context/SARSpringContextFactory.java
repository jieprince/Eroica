package com.sendtomoon.eroica.eoapp.sar.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.pingan.pafa.pola.Pola;
import com.sendtomoon.eroica.pizza.Pizza;
import com.sendtomoon.eroica.pizza.spring.PizzaResource;
import com.sendtomoon.eroica.eoapp.sar.SARContextAttrs;
import com.sendtomoon.eroica.eoapp.sar.SARDispatcher;
import com.sendtomoon.eroica.eoapp.web.WebDispatcher;

public class SARSpringContextFactory {

	protected Log logger = LogFactory.getLog(this.getClass());

	public static final String BEAN_NAME_DEF_DISPATCHER = "_sar_def_dispatcher";

	private static final String SAR_DEF_SPRING_XML = "META-INF/eroica/sar-default.spring.xml";

	private static final String KEY_PREFIX_SPRING = ".spring.xml";

	public static final String BEAN_ID_SAR_PROPERTIES = "_sar_properties";

	private SARDispatcher dispatcher;

	private WebDispatcher webDispatcher;

	protected ConfigurableApplicationContext newSpringContext(Resource[] resources, ClassLoader classLoader,
			SARContextAttrs attrs, ServletContext servletContext, Pola pola) {
		SARXmlApplicationContext context = new SARXmlApplicationContext(attrs.getSarName(), resources, pola,
				attrs.getBasePackages());
		context.setClassLoader(classLoader);
		context.setServletContext(servletContext);
		context.setConfigLocation("sar:*" + KEY_PREFIX_SPRING);
		return context;
	}

	public ConfigurableApplicationContext create(ApplicationContext app, ClassLoader classLoader,
			final SARContextAttrs attrs, ServletContext servletContext) {

		String sarName = attrs.getSarName();
		final Pola pola = app.getBean(Pola.class);
		//
		List<Resource> resourceList = new ArrayList<Resource>(8);
		resourceList.add(getDefResource(attrs));
		// -----
		Set<String> plugins = attrs.getPlugins();
		if (logger.isInfoEnabled()) {
			logger.info("SAR<" + sarName + "> plugins=" + plugins);
		}
		for (String plugin : plugins) {
			resourceList.add(new ClassPathResource("META-INF/eroica/sar/plugins/" + plugin + ".xml", classLoader));
		}
		///
		PizzaResource def = new PizzaResource(Pizza.GROUP_SAR + "/" + sarName + KEY_PREFIX_SPRING);
		if (def.exists()) {
			resourceList.add(def);
		}
		// --------------------------------------------------------
		Resource resourceArray[] = new Resource[resourceList.size()];
		resourceList.toArray(resourceArray);
		//
		if (logger.isInfoEnabled()) {
			logger.info("SAR<" + sarName + "> spring resources=" + Arrays.toString(resourceArray));
		}
		ConfigurableApplicationContext springContext = newSpringContext(resourceArray, classLoader, attrs,
				servletContext, pola);
		//
		springContext.setParent(app);
		springContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {

			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				beanFactory.registerSingleton(BEAN_ID_SAR_PROPERTIES, attrs);
			}
		});
		springContext.refresh();
		initBeans(springContext, attrs);
		return springContext;
	}

	protected void initBeans(ConfigurableApplicationContext springContext, final SARContextAttrs attrs) {
		String beanName = BEAN_NAME_DEF_DISPATCHER;
		dispatcher = springContext.getBean(beanName, SARDispatcher.class);
		try {
			webDispatcher = springContext.getBean(WebDispatcher.class);
		} catch (BeansException ex) {
		}
	}

	protected Resource getDefResource(SARContextAttrs attrs) {
		return new ClassPathResource(SAR_DEF_SPRING_XML);
	}

	public SARDispatcher getDispatcher() {
		return dispatcher;
	}

	public WebDispatcher getWebDispatcher() {
		return webDispatcher;
	}

}
