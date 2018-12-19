package com.sendtomoon.eroica.eoapp.protocol.dubbo.ws;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.sendtomoon.eroica.common.web.WebException;
import com.sendtomoon.eroica.dubbo.ws.IWeService;
import com.sendtomoon.eroica.dubbo.ws.WeService;
import com.sendtomoon.eroica.eoapp.protocol.dubbo.IDubboProtocol;
import com.sendtomoon.eroica.eoapp.sar.SARContext;
import com.sendtomoon.eroica.eoapp.sar.event.SAREvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARShutdownEvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARStartupFailedEvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARStartupedEvent;

public class WeServiceRegisterBean extends AbstractConfig
		implements ApplicationListener<SAREvent>, ServletContextAware {

	private static final long serialVersionUID = 1L;

	private IDubboProtocol dubboProtocol;

	private ServletContext servletContext;

	private Map<String, ServiceConfig<IWeService>> services = new ConcurrentHashMap<String, ServiceConfig<IWeService>>();

	private Map<String, Map<String, ServiceConfig<IWeService>>> sar_services = new ConcurrentHashMap<String, Map<String, ServiceConfig<IWeService>>>();

	protected List<ServiceConfig<IWeService>> scanClass(SARContext SARContext, Class<?> targetClazz, String beanName) {
		if (targetClazz.getAnnotation(Controller.class) == null) {
			return null;
		}
		List<ServiceConfig<IWeService>> services = null;
		Method[] methods = targetClazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			WeService annotation = method.getAnnotation(WeService.class);
			//
			if (annotation == null) {
				continue;
			}
			RequestMapping mapping = method.getAnnotation(RequestMapping.class);
			if (mapping == null) {
				throw new BeanDefinitionValidationException(
						"Bean<" + beanName + "> method<" + method.getName() + ">  defined error" + ",casue:Annotation:"
								+ RequestMapping.class.getName() + " not defined. ");
			}
			String[] paths = mapping.value();
			String path = null;
			if (paths.length == 0 || (path = paths[0]) == null || (path = path.trim()).length() == 0) {
				throw new BeanDefinitionValidationException(
						"Bean<" + beanName + "> method<" + method.getName() + ">  defined error" + ",casue:Annotation:"
								+ RequestMapping.class.getName() + " path requried. ");
			}
			if (path.charAt(0) != '/') {
				path = '/' + path;
			}
			ConfigurableListableBeanFactory beanFactory = SARContext.getSpringContext().getBeanFactory();
			path = this.resolveDefaultValue(beanFactory, path);
			String id = annotation.id();
			if ((id = id.trim()).length() == 0) {
				id = SARContext.getSARName() + path;
			} else {
				id = this.resolveDefaultValue(beanFactory, id);
				if (id.charAt(0) == '/') {
					id = id.substring(1);
				}
			}
			ServiceConfig<IWeService> service = new ServiceConfig<IWeService>();
			service.appendAnnotation(WeService.class, annotation);
			//
			service.setProvider(getProviderConfig(SARContext.getSARName(), annotation));
			//
			service.setRef(new WeServiceProvider(SARContext, path, servletContext));
			service.setId(id);
			service.setInterface(IWeService.class);
			if (services == null) {
				services = new ArrayList<ServiceConfig<IWeService>>();
			}
			services.add(service);
		}
		return services;
	}

	protected ProviderConfig getProviderConfig(String sarName, WeService annotation) {
		ApplicationConfig aplication = new ApplicationConfig();
		aplication.setName(sarName);
		ProviderConfig providerConfig = new ProviderConfig();
		providerConfig.setApplication(aplication);
		providerConfig.setRegistries(dubboProtocol.getRegistryConfig(annotation.registry()));
		providerConfig.setProtocols(dubboProtocol.getProtocolConfig(annotation.protocol()));
		return providerConfig;
	}

	protected String resolveDefaultValue(ConfigurableBeanFactory beanFactory, String value) {
		return beanFactory.resolveEmbeddedValue(value);
	}

	protected synchronized void onShutdowned(SAREvent event) {
		Map<String, ServiceConfig<IWeService>> configs = sar_services.remove(event.getSARName());
		if (configs == null || configs.size() == 0) {
			return;
		}
		for (String id : configs.keySet()) {
			ServiceConfig<IWeService> config = services.remove(id);
			config.unexport();
		}
	}

	protected synchronized void onStartuped(SAREvent event) {
		ConfigurableApplicationContext context = event.getSARSpringContext();
		String[] beanNames = context.getBeanDefinitionNames();
		Map<String, ServiceConfig<IWeService>> configs = new HashMap<String, ServiceConfig<IWeService>>();
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
			List<ServiceConfig<IWeService>> serviceConfigs = scanClass(event.getSARContext(), targetClass, beanName);
			if (serviceConfigs == null) {
				continue;
			}
			for (ServiceConfig<IWeService> config : serviceConfigs) {
				String id = config.getInterface();
				if (configs.containsKey(id)) {
					throw new WebException("WeService<" + id + "> repeated by SAR:" + event.getSARName());
				} else {
					configs.put(id, config);
				}
			}
		}
		if (configs == null || configs.size() == 0) {
			return;
		}
		for (String id : configs.keySet()) {
			if (services.containsKey(id)) {
				throw new WebException("WeService<" + id + "> repeated.");
			}
		}
		//
		sar_services.put(event.getSARName(), configs);
		for (String id : configs.keySet()) {
			ServiceConfig<IWeService> config = configs.get(id);
			config.export();
			this.services.put(id, config);
		}
	}

	@Override
	public void onApplicationEvent(SAREvent event) {
		if (event instanceof SARStartupedEvent) {
			this.onStartuped(event);
		} else if (event instanceof SARShutdownEvent || event instanceof SARStartupFailedEvent) {
			this.onShutdowned(event);
		}
	}

	public IDubboProtocol getDubboProtocol() {
		return dubboProtocol;
	}

	public void setDubboProtocol(IDubboProtocol dubboProtocol) {
		this.dubboProtocol = dubboProtocol;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
