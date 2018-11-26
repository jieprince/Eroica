package com.sendtomoon.eroica.eoapp.esa.client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.sendtomoon.eroica.common.Eroica;
import com.sendtomoon.eroica.common.appclient.DefaultActionClient;
import com.sendtomoon.eroica.common.appclient.IServiceClient;
import com.sendtomoon.eroica.common.appclient.annotation.ActionClient;

public class ESABeanPostProcessor extends ApplicationObjectSupport implements BeanPostProcessor, Ordered {

	private String defaultEroicaAc;

	private String defaultDataType = Eroica.DATA_TYPE_MAP;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> clazz = bean.getClass();
		Class<?> targetClass = clazz;
		do {
			for (Field field : targetClass.getDeclaredFields()) {
				ActionClient annotation = null;
				if ((annotation = field.getAnnotation(ActionClient.class)) != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@ActionClient annotation is not supported on static fields");
					}
					if (field.getType() != IServiceClient.class) {
						throw new IllegalStateException(
								"@ActionClient annotation requires on field type:" + IServiceClient.class.getName());
					}
					ReflectionUtils.makeAccessible(field);
					try {
						if (logger.isInfoEnabled()) {
							logger.info("Handle annotation<" + annotation + "> for Bean<" + beanName + "> field<"
									+ field.getName() + ">.");
						}
						field.set(bean, getActionClient(annotation));
					} catch (Exception e) {
						throw new InvalidPropertyException(clazz, field.getName(),
								"wire annotation:" + annotation + " error,cause:" + e.getMessage(), e);
					}
				}
			}
			for (Method method : targetClass.getDeclaredMethods()) {
				ActionClient annotation = method.getAnnotation(ActionClient.class);
				if (annotation != null) {
					method = BridgeMethodResolver.findBridgedMethod(method);
					Method mostSpecificMethod = BridgeMethodResolver
							.findBridgedMethod(ClassUtils.getMostSpecificMethod(method, clazz));
					if (method.equals(mostSpecificMethod) && method.isAnnotationPresent(ActionClient.class)) {

						try {
							if (Modifier.isStatic(method.getModifiers())) {
								throw new IllegalStateException(
										"@ActionClient annotation is not supported on static methods");
							}
							if (method.getParameterTypes().length != 1) {
								throw new IllegalStateException(
										"@ActionClient annotation requires a single-arg method: " + method);
							}
							if (method.getParameterTypes()[0] != IServiceClient.class) {
								throw new IllegalStateException("@ActionClient annotation requires method-arg be:"
										+ IServiceClient.class.getName());
							}
							ReflectionUtils.makeAccessible(method);
							if (logger.isInfoEnabled()) {
								logger.info("Handle annotation<" + annotation + "> for Bean<" + beanName + "> method<"
										+ method.getName() + ">.");
							}
							method.invoke(bean, getActionClient(annotation));
						} catch (Throwable e) {
							if (e instanceof InvocationTargetException) {
								e = ((InvocationTargetException) e).getTargetException();
							}
							throw new InvalidPropertyException(clazz, method.getName(),
									"wire annotation:" + annotation + " error,cause:" + e.getMessage(), e);
						}
					}
				}
			}
			targetClass = targetClass.getSuperclass();
		} while ((targetClass != null) && (targetClass != Object.class));
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}



	public String getDefaultEroicaAc() {
		return defaultEroicaAc;
	}

	public void setDefaultEroicaAc(String defaultEroicaAc) {
		this.defaultEroicaAc = defaultEroicaAc;
	}

	public String getDefaultDataType() {
		return defaultDataType;
	}

	public void setDefaultDataType(String defaultDataType) {
		this.defaultDataType = defaultDataType;
	}

	protected Object resolveDefaultValue(String value) {
		ApplicationContext context = this.getApplicationContext();
		if (context != null) {
			BeanFactory bf = context.getAutowireCapableBeanFactory();
			if (bf instanceof ConfigurableBeanFactory) {
				ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) bf;
				return cbf.resolveEmbeddedValue(value);
			}
		}
		return value;
	}

	protected IServiceClient getActionClient(ActionClient annotation) {
		DefaultActionClient client = new DefaultActionClient();
		// ------------------------------------------------------------
		String ac = annotation.pafaAc();
		if ((ac = ac.trim()).length() == 0) {
			ac = this.defaultEroicaAc;
		} else {
			ac = (String) this.resolveDefaultValue(ac);
		}
		// ----------
		if (ac == null || (ac = ac.trim()).length() == 0) {
			throw new NullPointerException("PafaAc is null");
		}
		client.setEroicaAc(ac);
		// ------------------------------------------------------------
		String actionName = annotation.name();
		if (actionName == null || (actionName = actionName.trim()).length() == 0) {
			throw new NullPointerException("actionName is null");
		}
		client.setName(actionName);
		String group = annotation.group();
		if (group != null && (group = group.trim()).length() > 0) {
			client.setGroup(group);
		}
		// -------------------------------------------------------------
		if (annotation != null && annotation.dataType().trim().length() > 0) {
			client.setDataType(annotation.dataType().trim());
		} else {
			client.setDataType(this.defaultDataType);
		}
		// ----------------------------------
		if (annotation != null) {
			client.setAsyncFlag(annotation.asyncFlag());
			client.setDisableLog(annotation.disableLog());
		}
		client.setApplicationContext(this.getApplicationContext());
		client.setHandleClazz(this.getClass());
		// -------------------------------
		try {
			client.afterPropertiesSet();
		} catch (Exception e) {
			throw new FatalBeanException("action:" + actionName + " define error,cause:" + e.getMessage(), e);
		}
		return client;
	}

}
