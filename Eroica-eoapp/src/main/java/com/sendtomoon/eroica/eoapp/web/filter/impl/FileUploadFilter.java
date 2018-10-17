package com.sendtomoon.eroica.eoapp.web.filter.impl;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.sendtomoon.eroica.eoapp.web.filter.AbstractWebFilter;

public class FileUploadFilter extends AbstractWebFilter {
	
	private MultipartResolver multipartResolver;
	

	public FileUploadFilter(){
		this.setPattern("/**");
		this.setOrder(-99999);
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest=(HttpServletRequest)request;
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(httpRequest)) {
			httpRequest=this.multipartResolver.resolveMultipart(httpRequest);
			try{
				//重写请求
				chain.doFilter(httpRequest, response);
			}finally{
				this.multipartResolver.cleanupMultipart((MultipartHttpServletRequest) httpRequest);
			}
		}else{
			chain.doFilter(request, response);
		}
	}


	public MultipartResolver getMultipartResolver() {
		return multipartResolver;
	}


	public void setMultipartResolver(MultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}
	
	

}
