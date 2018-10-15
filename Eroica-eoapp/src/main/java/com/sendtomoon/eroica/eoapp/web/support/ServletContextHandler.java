package com.sendtomoon.eroica.eoapp.web.support;

import java.util.Arrays;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.sendtomoon.eroica.eoapp.EoAppException;

public class ServletContextHandler {
	
	private Log logger=LogFactory.getLog(this.getClass());
	
	
	private ServletContext servletContext;
	
	private boolean resolved;
	
	public ServletContextHandler(ServletContext servletContext){
		this.servletContext=servletContext;
	}


	public  synchronized ServletContext resolveServletContext(ConfigurableListableBeanFactory beanFactory){
		if(resolved){
			return servletContext;
		}
		ServletContext resolvedServletContext=resolveServletContextByBeanFactory(beanFactory);
		if(resolvedServletContext==null){
			resolvedServletContext=this.servletContext;
		}else{
			this.servletContext=resolvedServletContext;
		}
		if(resolvedServletContext!=null && logger.isInfoEnabled()){
			logger.info("ServletContext="+resolvedServletContext);
		}
		if(resolvedServletContext==null){
			throw new EoAppException("ServletContext required.");
		}
		this.resolved=true;
		return resolvedServletContext;
	}
	
	protected ServletContext resolveServletContextByBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		ServletContext servletContext = null;
		try{
			servletContext=beanFactory.getBean(ServletContext.class);
			return servletContext;
		}catch(NoSuchBeanDefinitionException ex){
			//
		}
		String beanNames[] = beanFactory.getBeanNamesForType(ServletContextResolver.class);
		if (beanNames != null && beanNames.length > 0) {
			if (beanNames.length == 1) {
				servletContext = beanFactory.getBean(beanNames[0], ServletContextResolver.class)
						.resolveServletContext();
				if(servletContext==null){
					throw new EoAppException("Can't resolve servletContext,cause bean:"
							+ beanNames[0] + " method:resolveServletContext() return null.");
				}
				if(logger.isInfoEnabled()){
					logger.info("ResolvedServletContext:"+servletContext+" by bean:"+beanNames[0]);
				}
			} else {
				throw new EoAppException("Can't resolve servletContext,cause found many beans:"
						+ Arrays.toString(beanNames) + " for interface:" + ServletContextResolver.class.getName());
			}
		}
		return servletContext;
	}
	
}
