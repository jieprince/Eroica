package com.sendtomoon.eroica.eoapp.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sendtomoon.eroica.eoapp.sar.SARContext;

public class EroicaWebRequestHandler implements WebRequestHandler{
	
	private volatile List<SARContext> sars;
	
	private volatile Map<String,String> requestSARMapping;
	
	private volatile String requestURI;
	
	
	public EroicaWebRequestHandler(List<SARContext> sars,Map<String,String> requestSARMapping,String requestURI){
		this.sars=sars;
		this.requestSARMapping=requestSARMapping;
		this.requestURI=requestURI;
	}


	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SARContext sar=resolveSAR();
		if(sar!=null){
			sar.handleWebRequest(request, response);
			return;
		}
		if(sars!=null && sars.size()>0){
			for(int i=0;i<sars.size();i++){
				sar=sars.get(i);
				if(sar!=null && sar.handleWebRequest(request, response)){
					requestSARMapping.put(requestURI, sar.getSARName());
					return ;
				}
			}
		}
		noHandlerFound(request,response);
	}
	
	protected SARContext resolveSAR(){
		String sarName=requestSARMapping.get(requestURI);
		if(sarName==null){
			return null;
		}
		List<SARContext> sars=this.sars;
		if(sars==null || sars.size()==0){
			return null;
		}
		for(int i=0;i<sars.size();i++){
			SARContext sar=sars.get(i);
			if(sarName.equals(sar.getSARName())){
				return sar;
			}
		}
		return null;
	}
	
	protected static final Log pageNotFoundLogger = LogFactory.getLog("PEoAppNotFoundLogger");
	
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
		pageNotFoundLogger.error("No handler found for HTTP request with URI [" + request.getRequestURI() +
				"]!");
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	
}
