package com.sendtomoon.eroica.eoapp.web.filter.impl;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;

import com.sendtomoon.eroica.common.web.util.ContentTypeUtil;
import com.sendtomoon.eroica.common.web.util.JsonHttpServletRequestWrapper;
import com.sendtomoon.eroica.eoapp.web.filter.AbstractWebFilter;

public class PostJsonToFormFilter extends AbstractWebFilter{
	
	private MediaType _supportedMediaType=MediaType.APPLICATION_JSON;
	
	private String supportedMediaType;
	
	private boolean enable=true;
	
	private ContentTypeUtil contentTypeUtil=new ContentTypeUtil();
	

	public PostJsonToFormFilter(){
		this.setPattern("/**");
		this.setOrder(-99999);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest=(HttpServletRequest)request;
		if(enable && support(httpRequest)){
			httpRequest=new JsonHttpServletRequestWrapper(httpRequest);
		}
		chain.doFilter(httpRequest, response);
	}
	
	public boolean support(HttpServletRequest request) {
		if("POST".equals(request.getMethod())){
			MediaType contentType=contentTypeUtil.getMediaType(request);
			if(_supportedMediaType.includes(contentType)){
				return true;
			}
		}
		return false;
	}
	
	

	public void setSupportedMediaType(String supportedMediaType) {
		this.supportedMediaType =supportedMediaType;
	}



	public String getSupportedMediaType() {
		return supportedMediaType;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		if(supportedMediaType!=null){
			_supportedMediaType= MediaType.parseMediaType(supportedMediaType);
		}
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	
}
