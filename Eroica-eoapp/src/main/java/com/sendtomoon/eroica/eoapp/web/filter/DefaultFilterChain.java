package com.sendtomoon.eroica.eoapp.web.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.NestedServletException;

import com.sendtomoon.eroica.eoapp.web.WebRequestHandler;

public final class DefaultFilterChain implements FilterChain{
	
	protected static Log logger=LogFactory.getLog(FilterChain.class);
	
	private volatile List<WebFilter> webFilters;
	
	protected static final PathMatcher pathMatcher=new AntPathMatcher();
	
	
	private volatile WebRequestHandler handler;
	
	
	private volatile String requestURI;
	
	private volatile int curIndex;
	
	
	private volatile HttpServletRequest processedRequest;
	
	
	
	
	public DefaultFilterChain(List<WebFilter> webFilters,HttpServletRequest request
			,WebRequestHandler handler,String requestURI){
		this.webFilters=webFilters;
		this.handler=handler;
		this.requestURI=requestURI;
		processedRequest=request;
	}
	 

	@Override
	public final void doFilter(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		if(request!=null){
			processedRequest=(HttpServletRequest)request;
		}
		while(curIndex!=webFilters.size()){
			WebFilter filter=webFilters.get(curIndex);
			curIndex++;
			//--------------
			if(matchRequest(filter,requestURI)){
				filter.doFilter(request, response, this);
				//注意无条件返回
				return ;
			}
		}
		doFinal(request,response);
	}

	protected void doFinal(ServletRequest request, ServletResponse response) throws ServletException,IOException{
		try {
			handler.handleRequest((HttpServletRequest)request, (HttpServletResponse)response);
		}catch(ServletException e){
			throw e;
		}catch(IOException e){
			throw e;
		}catch (RuntimeException e) {
			throw e;
		}catch (Exception e) {
			throw new NestedServletException(e.getMessage(),e);
		}
	}

	protected final boolean matchRequest(WebFilter filter,String requestURI){
		List<String> patterns=filter.getPatterns();
		if(patterns==null || patterns.size()==0){
			//没有指定
			return false;
		}
		for(int i=0;i<patterns.size();i++){
			//匹配
			String urlPattern = (String) patterns.get(i);
			if (urlPattern!=null && pathMatcher.match(urlPattern, requestURI)) {
				if(logger.isDebugEnabled()){
					logger.debug("Request:"+requestURI+" matched filter:"+filter.getClass().getName()+" by parttern:"+urlPattern);
				}
				return true;
			}
		}
		return false;
	}


	public HttpServletRequest getProcessedRequest() {
		return processedRequest;
	}


	

	
	
}
