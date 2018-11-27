package com.sendtomoon.eroica.dubbo.ws.provider;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.ac.dubbo.AbstractGenericService;
import com.sendtomoon.eroica.ac.dubbo.GenericResult;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.dubbo.DubboGenericException;
import com.sendtomoon.eroica.dubbo.ws.WeServiceRequest;
import com.sendtomoon.eroica.dubbo.ws.WeServiceResponse;

public abstract class AbstractWebGenericService extends AbstractGenericService implements GenericService{
	
	protected  Log logger=LogFactory.getLog(this.getClass());
	
	private ServletContext servletContext;
	
	public static final String KEY_PATH="path";

	
	private String mappingPath;
	
	public AbstractWebGenericService(ServletContext servletContext,String mappingPath){
		this.mappingPath=mappingPath;
		this.servletContext=servletContext;
	}
	
	
	@Override
	public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
		
		RpcContext rpcContext=RpcContext.getContext();
		String interfaceName=rpcContext.getAttachment("interface");
		if(interfaceName==null){
			interfaceName=rpcContext.getUrl().getPath();
		}
		if(interfaceName==null){
			throw new java.lang.IllegalArgumentException("Requried <interface>  in RpcContext.");
		}
		if(mappingPath==null || mappingPath.length()==0){
			throw new DubboGenericException("Interface<"+interfaceName+"> not supported WeService."
					+"mappingPath requried,defined by:"
					+org.springframework.web.bind.annotation.RequestMapping.class.getName());
		}
		HttpServletRequest request=getRequest(interfaceName,args);
		WeServiceResponse response=new WeServiceResponse();
		try {
			handleRequest(request, response);
		} catch (ServletException e) {
			throw new DubboGenericException(e.getMessage(),e);
		}
		return toGenericResult(response.toModel());
	}
	
	protected HttpServletRequest getRequest(String interfaceName,Object[] args){
		Map params=this.getParams(args);
		if(logger.isDebugEnabled()){
			logger.debug("Handle interface<"+interfaceName+">.");
		}
		params.put(KEY_PATH, mappingPath);
		forSessionDTO(new ServiceRequest(interfaceName,params));
		return new WeServiceRequest(params,servletContext);
	}
	
	protected GenericResult toGenericResult(Map<Object,Object> model) {
		return new GenericResult(model);
	}
	


	protected abstract void handleRequest(HttpServletRequest request,HttpServletResponse response) throws ServletException;

}
