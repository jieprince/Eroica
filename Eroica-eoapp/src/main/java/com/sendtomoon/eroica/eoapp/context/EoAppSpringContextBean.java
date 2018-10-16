package com.sendtomoon.eroica.eoapp.context;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;

import com.sendtomoon.eroica.eoapp.EoAppException;
import com.sendtomoon.eroica.eoapp.context.support.ESAManager;
import com.sendtomoon.eroica.eoapp.context.support.SARManager;
import com.sendtomoon.eroica.eoapp.web.EoAppWebDispatcher;

public class EoAppSpringContextBean implements EoAppSpringContext {

	protected Log logger = LogFactory.getLog(this.getClass());

	private volatile EoAppWebDispatcher webDispatcher;

	private volatile SARManager sarManager;

	private volatile ESAManager esaManager;

	private static final String DEF_WEB_DISPATCHER_BEAN_NAME = "_eoapp_web_def_dispatcher";

	private volatile ConfigurableApplicationContext applicationContext = null;

	private ServletContext servletContext;

	public EoAppSpringContextBean(ConfigurableApplicationContext applicationContext, String webDispatcherBeanId,
			ServletContext servletContext) {
		this.applicationContext = applicationContext;
		this.servletContext = servletContext;
		initBeans(applicationContext, webDispatcherBeanId);
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected void initBeans(ConfigurableApplicationContext applicationContext, String webDispatcher) {
		forWebDispacher(applicationContext, webDispatcher);
		sarManager = applicationContext.getBean(SARManager.class);
		esaManager = applicationContext.getBean(ESAManager.class);
	}

	protected void forWebDispacher(ConfigurableApplicationContext applicationContext, String beanName) {
		if (beanName == null || (beanName = beanName.trim()).length() == 0) {
			try {
				webDispatcher = applicationContext.getBean(DEF_WEB_DISPATCHER_BEAN_NAME, EoAppWebDispatcher.class);
			} catch (BeansException ex) {
			}
		} else {
			try {
				webDispatcher = applicationContext.getBean(beanName, EoAppWebDispatcher.class);
			} catch (BeansException ex) {
				throw new EoAppException("Not found default web request dispatcher,cause:" + ex.getMessage(), ex);
			}
		}

	}

	public EoAppWebDispatcher getWebDispatcher() {
		if (webDispatcher == null) {
			throw new EoAppException("Not found default web request dispatcher.");
		}
		return webDispatcher;
	}

	public void close() {
		this.applicationContext.close();
		applicationContext = null;
	}

	public SARManager getSarManager() {
		return sarManager;
	}

	public ESAManager getEsaManager() {
		return esaManager;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

}
