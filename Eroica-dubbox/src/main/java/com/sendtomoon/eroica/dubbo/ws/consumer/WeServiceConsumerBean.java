package com.sendtomoon.eroica.dubbo.ws.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.sendtomoon.eroica.dubbo.ws.IWeService;
import com.sendtomoon.eroica.dubbo.ws.WeServiceRequest;
import com.sendtomoon.eroica.dubbo.ws.WeServiceResponse;

public class WeServiceConsumerBean implements WeServiceConsumer{
	
	protected Log logger=LogFactory.getLog(this.getClass());
	
	private Map<String,ReferenceConfig<IWeService>> servicesMapping;
	
	protected  final Lock lock = new ReentrantLock();
	
	private ConsumerConfig consumerConfig;
	
	private String consumerName;

	@Override
	public void handleRequest(String serviceId, HttpServletRequest request, HttpServletResponse response) {
		IWeService consumer=getConsumerReference(serviceId,null);
     	try {
     		WeServiceRequest wsRequest=new WeServiceRequest(request,serviceId);
         	Map<Object,Object> result =consumer.$invoke(wsRequest.toModel());
			new WeServiceResponse(result).write(response);
		} catch (Exception e) {
			throw new WebServiceException(e.getMessage(),e);
		}
	}
	
	
	
	
	protected IWeService getConsumerReference(String serviceId,String groupId){
		if(servicesMapping==null){
			throw new java.lang.IllegalStateException("Be stoped.");
		}
		lock.lock();
		ReferenceConfig<IWeService> reference=null;
		try{
			if(serviceId==null || serviceId.length()==0){
				throw new NullPointerException("serviceId requried.");
			}
			reference=servicesMapping.get(serviceId);
			if(reference==null){
				reference = new ReferenceConfig<IWeService>();
				reference.setId(serviceId);
				reference.setInterface(IWeService.class);
				reference.setGroup(groupId);
				reference.setConsumer(getConsumerConfig());
				//
				IWeService service=reference.get();
				if(service!=null){
					servicesMapping.put(serviceId, reference);	
				}else{
					reference.destroy();
				}
			}
		}finally{
			lock.unlock();
		}
		return (IWeService)reference.get();
	}
	
	@PreDestroy
	public void shutdown(){
		//
		Map<String,ReferenceConfig<IWeService>> mapping=servicesMapping;
		this.servicesMapping=null;
		//
		if(mapping==null){
			return ;
		}
		String serviceIds[]=new String[mapping.size()];
		mapping.keySet().toArray(serviceIds);
		for(String id:serviceIds){
			ReferenceConfig<IWeService> ref=mapping.get(id);
			try{
				if(ref!=null){
					ref.destroy();
				}
			}catch(Throwable th){
				logger.error(th.getMessage(),th);
			}
		}
	}
	
	@PostConstruct
	public void init() throws Exception {
		servicesMapping=new ConcurrentHashMap<String,ReferenceConfig<IWeService>>();
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

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}

	public String getConsumerName() {
		return consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}
	
	
	
}
