package com.sendtomoon.eroica.eoapp.web.support;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sendtomoon.eroica.common.web.WebException;

public class FastJsonModelAndViewResolver implements HandlerMethodReturnValueHandler,Ordered{
	
	private String defaultContentType;
	
	private Log logger=LogFactory.getLog(this.getClass());
	
	
	private boolean logable=true;
	
	private boolean enableReturnEmpty=false;
	
	private boolean enbale=true;
	
	
	
	

	@Override
	public boolean supportsReturnType(MethodParameter methodParameter) {
		return this.isEnbale() && methodParameter.getMethod().getAnnotation(ResponseBody.class)!=null;
	}


	@Override
	public void handleReturnValue(Object returnValue,
			MethodParameter methodParameter,
			ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest request) throws Exception {
		HttpServletResponse response=request.getNativeResponse(HttpServletResponse.class);
		writeResult(returnValue,response);
	}


	public void writeResult(Object returnValue,HttpServletResponse response)  {
		try {
			if(defaultContentType!=null){
				response.setContentType(defaultContentType);
			}else{
				if(!StringUtils.hasText(response.getContentType())){
					response.setContentType("application/json");
				}
			}
			String resp=resultToString(returnValue);
			Writer out=response.getWriter();
			if(logable && logger.isDebugEnabled()){
				logger.debug("ResponseContent="+resp);
			}
			out.write(resp);
			out.flush();
		} catch (IOException e) {
			throw new WebException(e.getMessage(),e);
		}
		
	}
	
	protected String resultToString(Object returnValue){
		String resp=null;
		if(returnValue!=null && returnValue instanceof ModelAndView){
			returnValue=((ModelAndView)returnValue).getModel();
		}
		if(returnValue==null){
			resp=enableReturnEmpty?"":"{}";
		}else if(returnValue instanceof String || returnValue instanceof Number || returnValue instanceof Boolean ){
			resp=returnValue.toString();
		}else if(returnValue.getClass().isArray() || returnValue instanceof Collection){
			Map<Object,Object> model=new HashMap<Object,Object> ();
			model.put("responseCode","0");
			model.put("datas",returnValue);
			resp=beanResultToString(model);
		}else{
			resp=beanResultToString(returnValue);
		}
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	protected String beanResultToString(Object returnValue){
		if (returnValue instanceof ResponseEntity) {
			ResponseEntity<Object> entity = (ResponseEntity<Object>) returnValue;
			Object body = entity.getBody();
			return JSON.toJSONString(body);
		}else {
			return JSON.toJSONString(returnValue);
		}
	}
	
	

	public String getDefaultContentType() {
		return defaultContentType;
	}

	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
	}

	

	public boolean isLogable() {
		return logable;
	}

	public void setLogable(boolean logable) {
		this.logable = logable;
	}


	public Log getLogger() {
		return logger;
	}


	public void setLogger(Log logger) {
		this.logger = logger;
	}


	public boolean isEnableReturnEmpty() {
		return enableReturnEmpty;
	}


	public void setEnableReturnEmpty(boolean enableReturnEmpty) {
		this.enableReturnEmpty = enableReturnEmpty;
	}


	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}


	public boolean isEnbale() {
		return enbale;
	}


	public void setEnbale(boolean enbale) {
		this.enbale = enbale;
	}

	
	
	
}
