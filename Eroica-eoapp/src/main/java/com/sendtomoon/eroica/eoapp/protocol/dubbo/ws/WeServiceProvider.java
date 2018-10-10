package com.sendtomoon.eroica.eoapp.protocol.dubbo.ws;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.sendtomoon.eroica.dubbo.ws.IWeService;
import com.sendtomoon.eroica.dubbo.ws.WeServiceRequest;
import com.sendtomoon.eroica.dubbo.ws.WeServiceResponse;
import com.sendtomoon.eroica.eoapp.sar.SARContext;

 class WeServiceProvider implements IWeService {
	
	private SARContext SARContext;
	
	private String path;
	
	private ServletContext servletContext;
	
	
	public WeServiceProvider(SARContext SARContext,String path,ServletContext servletContext){
		this.SARContext=SARContext;
		this.path=path;
		this.servletContext=servletContext;
	}

	@Override
	public Map<Object, Object> $invoke(Map<Object, Object> params) 
			throws ServletException {
		params.put("path", path);
		WeServiceRequest request=new WeServiceRequest(params,servletContext);
		WeServiceResponse response=new WeServiceResponse();
		SARContext.handleWebRequest(request, response);
		return response.toModel();
	}
	
}
