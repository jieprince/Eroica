package com.sendtomoon.eroica.appclient.dubbo;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.ac.dubbo.GenericParam;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationController;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerException;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.utils.ESAPatternUtils;

public class ApplicationControllerDubboClient extends DubboServiceRequestHandler implements ApplicationController ,InitializingBean,DisposableBean/*,Lifecycle,Phased*/{
	
	protected Log logger=LogFactory.getLog(this.getClass());
	
	private Map<String,ReferenceConfig<GenericService>> referenceConfigMapping;
	
	protected 	 final Lock lock = new ReentrantLock();
	
	private ConsumerConfig consumerConfig;
	
	private String consumerName;
	
	@Override
	public ServiceResponse handleRequest(ServiceRequest request) throws ApplicationControllerException, RemoteException {
		String actionName=request.getRequestedServiceID();
		GenericService gs = null;
		//获取dubbo context上下文
		RpcContext context = RpcContext.getContext();
		//获取返回=false参数
		Object return_key = context.get(Constants.RETURN_KEY);
		//获取async=true的参数
		Object async_key = context.get(Constants.ASYNC_KEY);
		
		if ((Boolean.FALSE.toString().equals(return_key))||(Boolean.TRUE.toString().equals(async_key))) {
			gs = getAsyncGenericService(actionName, request.getGroup());
		} else {
			gs = getGenericService(actionName, request.getGroup());
		}
		//不再支持异步调用客户端自适应，需要在接口注册的时候配置scop=remote
		//GenericService gs = getGenericService(actionName, request.getGroup());
		return handleRequest(GenericParam.REQUEST_TYPE_DEF,request,gs);
	}
	
	protected GenericService getGenericService(String actionName,String group){
		if(referenceConfigMapping==null){
			throw new java.lang.IllegalStateException("Be stoped.");
		}
		if(actionName==null || actionName.length()==0){
			throw new NullPointerException("actionName is null.");
		}
		ESAPatternUtils.check(actionName);
		//
		String key=(group==null?"":group+"/")+actionName;
		ReferenceConfig<GenericService> reference=referenceConfigMapping.get(key);
		if(reference!=null){
			return (GenericService)reference.get();
		}
		//
		lock.lock();
		try{
			reference=referenceConfigMapping.get(key);
			if(reference==null){
				reference=createReferenceConfig(actionName,group);
				GenericService service=reference.get();
				if(service!=null && referenceConfigMapping!=null){
					referenceConfigMapping.put(key, reference);	
				}else{
					reference.destroy();
				}
			}
			return (GenericService)reference.get();
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * 生成客户端异步调用dubbo reference对象
	 * @param actionName
	 * @param group
	 * @return
	 */
	protected GenericService getAsyncGenericService(String actionName,String group){
		if(referenceConfigMapping==null){
			throw new java.lang.IllegalStateException("Be stoped.");
		}
		if(actionName==null || actionName.length()==0){
			throw new NullPointerException("actionName is null.");
		}
		ESAPatternUtils.check(actionName);
		//
		String key=(group==null?"":group+"/")+actionName +"/async";
		ReferenceConfig<GenericService> reference=referenceConfigMapping.get(key);
		if(reference!=null){
			return (GenericService)reference.get();
		}
		//
		lock.lock();
		try{
			reference=referenceConfigMapping.get(key);
			if(reference==null){
				reference=createReferenceConfig(actionName,group);
				reference.setScope("remote");
				
				GenericService service=reference.get();
				if(service!=null && referenceConfigMapping!=null){
					referenceConfigMapping.put(key, reference);	
				}else{
					reference.destroy();
				}
			}
			return (GenericService)reference.get();
		}finally{
			lock.unlock();
		}
	}
	
	protected ReferenceConfig<GenericService> createReferenceConfig(String actionName,String group){
		ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
		reference.setGeneric(true);
		reference.setInterface(actionName);
		if(group!=null) {
			reference.setGroup(group);
		}
		
		reference.setConsumer(getConsumerConfig());
		return reference;
	}
	

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		referenceConfigMapping=new ConcurrentHashMap<String,ReferenceConfig<GenericService>>();
		//
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
	
	

	@Override
	public void destroy() {
		//
		lock.lock();
		try{
			Map<String,ReferenceConfig<GenericService>> mapping=referenceConfigMapping;
			this.referenceConfigMapping=null;
			//
			if(mapping!=null){
				String actionNames[]=new String[mapping.size()];
				mapping.keySet().toArray(actionNames);
				for(String name:actionNames){
					ReferenceConfig<GenericService> obj=mapping.get(name);
					try{
						if(obj!=null){
							obj.destroy();
						}
					}catch(Throwable th){
						logger.error(th.getMessage(),th);
					}
				}
			}
		}finally{
			lock.unlock();
		}
	}

	public String getConsumerName() {
		return consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}
	
}
