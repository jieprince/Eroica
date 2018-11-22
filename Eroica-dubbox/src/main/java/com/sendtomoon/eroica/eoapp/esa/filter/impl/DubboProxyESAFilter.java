package com.sendtomoon.eroica.eoapp.esa.filter.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationController;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.esa.filter.AbstractESAFilter;
import com.sendtomoon.eroica.eoapp.esa.filter.ESAFilterChain;


public class DubboProxyESAFilter   extends AbstractESAFilter implements ApplicationContextAware,InitializingBean{
	
	
	private String dubboPafaAc;
	
	private final Lock lock = new ReentrantLock();
	
	private ApplicationContext applicationContext;
	
	private boolean  localPriority=true;
	
	private ApplicationController bean;
	
	public DubboProxyESAFilter(){}
	
	
	@Override
	public ServiceResponse doFilter(ServiceRequest request, ESAFilterChain chain)
			throws Throwable {
		return getDubboPafaAcBean().handleRequest(request);
	}
	
	
	protected ApplicationController getDubboPafaAcBean(){
		if(bean==null){
			lock.lock();
			try{
				if(bean==null){
					bean=applicationContext.getBean(dubboPafaAc, ApplicationController.class);
				}
			}finally{
				lock.unlock();
			}
		}
		return bean;
	}
	
	

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dubboPafaAc, "dubboPafaAc beanName required.");
		if(!applicationContext.containsBeanDefinition(dubboPafaAc)){
			throw new FatalBeanException("dubboPafaAc bean<"+dubboPafaAc+"> not defined.");
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext=applicationContext;
	}

	public String getDubboPafaAc() {
		return dubboPafaAc;
	}

	public void setDubboPafaAc(String dubboPafaAc) {
		this.dubboPafaAc = dubboPafaAc;
	}


	public boolean isLocalPriority() {
		return localPriority;
	}


	public void setLocalPriority(boolean localPriority) {
		this.localPriority = localPriority;
	}

}
