package com.sendtomoon.eroica.eoapp.web.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.context.ServletContextAware;

public class ServletContextBeanPostProcessor implements BeanPostProcessor{
	
	
	private ServletContextHandler servletContextHandler;
	
	public ServletContextBeanPostProcessor(){
		
	}
	
	private ConfigurableListableBeanFactory beanFactory;
	
	public ServletContextBeanPostProcessor(ServletContextHandler servletContextHandler,ConfigurableListableBeanFactory beanFactory){
		this.servletContextHandler=servletContextHandler;
		this.beanFactory=beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean,
			String beanName) throws BeansException {
		if(bean instanceof ServletContextAware){
			((ServletContextAware)bean).setServletContext(servletContextHandler.resolveServletContext(beanFactory));
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean,
			String beanName) throws BeansException {
		return bean;
	}

	
}
