package com.sendtomoon.eroica.eoapp.esa.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.ac.dubbo.GenericParam;
import com.sendtomoon.eroica.appclient.dubbo.DubboServiceRequestHandler;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.exception.EroicaException;
import com.sendtomoon.eroica.common.utils.ESAPatternUtils;
import com.sendtomoon.eroica.dubbo.ws.WeServiceRequest;
import com.sendtomoon.eroica.dubbo.ws.WeServiceResponse;

public class ESAWebClientBean extends DubboServiceRequestHandler implements ESAWebClient{
	
	
	protected Log logger=LogFactory.getLog(this.getClass());
	
	private Map<String,ReferenceConfig<GenericService>> referenceMapping;
	
	protected 	 final Lock lock = new ReentrantLock();
	
	private ConsumerConfig consumerConfig;
	
	private String consumerName;
	

	@Override
	public void handleRequest(String esaName, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		GenericService gs=getGenericService(esaName);
		//
		WeServiceRequest wsRequest=new WeServiceRequest(httpRequest,null);
		ServiceRequest request=new ServiceRequest(esaName,wsRequest.toModel());
		ServiceResponse response;
		try {
			response = handleRequest(GenericParam.REQUEST_TYPE_WEB,request,gs);
			Map<Object,Object> model=response.getModel();
			new WeServiceResponse(model).write(httpResponse);
		} catch (Exception e) {
			Throwable ex=(e.getCause()==null?e:e.getCause());
			throw new EroicaException(ex.getMessage(),ex);
		}
	}
	
	
	protected GenericService getGenericService(String actionName){
		if(referenceMapping==null){
			throw new java.lang.IllegalStateException("Be stoped.");
		}
		lock.lock();
		ReferenceConfig<GenericService> reference=null;
		try{
			if(actionName==null){
				throw new NullPointerException("actionName is null.");
			}
			ESAPatternUtils.check(actionName);
			//
			reference=referenceMapping.get(actionName);
			if(reference!=null){
				return reference.get();
			}
			reference = new ReferenceConfig<GenericService>();
			reference.setGeneric(true);
			reference.setInterface(actionName);
			reference.setConsumer(getConsumerConfig());
			//
			GenericService service=reference.get();
			if(service!=null){
				referenceMapping.put(actionName, reference);	
			}else{
				reference.destroy();
			}
			return service;
		}finally{
			lock.unlock();
		}
		
	}

	


	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}


	@PostConstruct
	public void init() throws Exception {
		referenceMapping=new ConcurrentHashMap<String,ReferenceConfig<GenericService>>();
		ConsumerConfig consumerConfig=this.getConsumerConfig();
		String consumerName=this.getConsumerName();
		//
		if(consumerConfig==null && consumerName!=null){
			consumerConfig=new ConsumerConfig();
			ApplicationConfig appConfig=new ApplicationConfig();
			appConfig.setName(consumerName);
			consumerConfig.setApplication(appConfig);
			this.setConsumerConfig(consumerConfig);
		}
	}
	
	

	@PreDestroy
	public void destroy() {
		//
		Map<String,ReferenceConfig<GenericService>> mapping=referenceMapping;
		this.referenceMapping=null;
		//
		if(mapping==null){
			return ;
		}
		String actionNames[]=new String[mapping.size()];
		mapping.keySet().toArray(actionNames);
		for(String name:actionNames){
			ReferenceConfig<GenericService> ref=mapping.get(name);
			try{
				if(ref!=null){
					ref.destroy();
				}
			}catch(Throwable th){
				logger.error(th.getMessage(),th);
			}
		}
	}

	public String getConsumerName() {
		return consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

}
