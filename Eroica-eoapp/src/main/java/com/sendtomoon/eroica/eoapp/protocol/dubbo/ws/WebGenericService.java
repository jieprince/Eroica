package com.sendtomoon.eroica.eoapp.protocol.dubbo.ws;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.ac.dubbo.GenericParam;
import com.sendtomoon.eroica.ac.dubbo.PafaAcGenericService;
import com.sendtomoon.eroica.dubbo.ws.provider.AbstractWebGenericService;
import com.sendtomoon.eroica.eoapp.sar.SARContext;

public class WebGenericService extends AbstractWebGenericService{
	
	protected  Log logger=LogFactory.getLog(this.getClass());
	
	private SARContext context;
	
	private GenericService defGenericService;
	
	public WebGenericService(SARContext context,ServletContext servletContext,String mappingPath){
		super(servletContext,mappingPath);
		this.context=context;
		defGenericService=new PafaAcGenericService(context);
	}
	
	@Override
	public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
		if(method.equals(GenericParam.REQUEST_TYPE_WEB)){
			return super.$invoke(method, parameterTypes, args);
		}else{
			return defGenericService.$invoke(method, parameterTypes, args);
		}
	}

	@Override
	protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		context.handleWebRequest(request, response);
	}
	
	

}
