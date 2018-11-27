package com.sendtomoon.eroica.eoapp.esa.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sendtomoon.eroica.common.app.biz.action.Action;
import com.sendtomoon.eroica.common.beans.map.BeanMapUtils;
import com.sendtomoon.eroica.eoapp.ESA;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;

public class MethodESAHandleBean extends MethodESADispatcher implements ApplicationListener<ContextRefreshedEvent> {

	private BeanMapUtils beanMapUtils = new BeanMapUtils();

	protected void processClass(ConfigurableBeanFactory beanFactory, Class<?> targetClazz, String beanName) {
		if (targetClazz.isAssignableFrom(Action.class)) {
			return;
		}
		Method[] methods = targetClazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			ESA annotation = method.getAnnotation(ESA.class);
			if (annotation == null) {
				continue;
			}
			String esaName = annotation.value();
			if (esaName.length() == 0) {
				esaName = annotation.name();
			}
			if ((esaName = esaName.trim()).length() == 0) {
				throw new BeanDefinitionValidationException(
						"Bean<" + beanName + "> method<" + method.getName() + "> ESA<" + esaName + ">"
								+ " defined error,casue: ESA annotation defined error: name required.");
			}
			esaName = beanFactory.resolveEmbeddedValue(esaName);
			MethodESA maa = null;
			try {
				maa = new MethodESAFactory(esaName, beanName, method, targetClazz, beanMapUtils).getObject();
			} catch (Exception e) {
				throw new BeanInitializationException(
						"Handle ESA:" + esaName + " configure error by bean:" + beanName + ",cause:" + e.getMessage(),
						e);
			}
			this.addESA(maa.getName(), maa);
			Properties config = ESAConfigUtils.getESAConfig(annotation);
			RequestMapping mapping = method.getAnnotation(RequestMapping.class);
			if (mapping != null) {
				String[] paths = mapping.value();
				String path = null;
				if (paths.length == 0 || (path = paths[0]) == null || (path = path.trim()).length() == 0) {
					throw new BeanDefinitionValidationException(
							"Bean<" + beanName + "> method<" + method.getName() + ">  defined error"
									+ ",casue:Annotation:" + RequestMapping.class.getName() + " path requried. ");
				}
				if (path.charAt(0) != '/') {
					path = '/' + path;
				}
				config.put(ESADefinition.WEB_MAPPING_PATH, path);
			}
			addConfig(esaName, config);
		}
	}

	protected Object resolveDefaultValue(ConfigurableBeanFactory beanFactory, String value) {
		return beanFactory.resolveEmbeddedValue(value);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ConfigurableApplicationContext context = (ConfigurableApplicationContext) event.getApplicationContext();
		ConfigurableBeanFactory beanFactory = ((ConfigurableApplicationContext) event.getApplicationContext())
				.getBeanFactory();
		String[] beanNames = context.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			Class<?> targetClass = context.getType(beanName);
			if (targetClass == null) {
				continue;
			}
			if (ClassUtils.isCglibProxyClass(targetClass)) {
				targetClass = targetClass.getSuperclass();
			} else if (Proxy.isProxyClass(targetClass)) {
				targetClass = AopUtils.getTargetClass(context.getBean(beanName));
			}
			processClass(beanFactory, targetClass, beanName);
		}
	}

}
