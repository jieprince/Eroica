package com.sendtomoon.eroica.eoapp.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.UrlPathHelper;

import com.sendtomoon.eroica.eoapp.context.support.SARManager;
import com.sendtomoon.eroica.eoapp.sar.SARContext;
import com.sendtomoon.eroica.eoapp.web.filter.DefaultFilterChain;
import com.sendtomoon.eroica.eoapp.web.filter.WebFilter;

public class DefEoAppWebDispatcher implements EoAppWebDispatcher,InitializingBean,ApplicationContextAware{

	
	private List<HandlerExceptionResolver> handlerExceptionResolvers;
	
	private List<WebFilter> webFilters;
	
	private ApplicationContext applicationContext;
	
	private volatile Map<String,String> requestSARMapping=new ConcurrentHashMap<String, String>();
	
	protected static final UrlPathHelper urlPathHelper = new UrlPathHelper();
	
	private SARManager sarManager;
	
	private volatile List<WebSAResolver> webSAResolvers;
	
	
	protected Log logger=LogFactory.getLog(this.getClass());
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext=applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, HandlerExceptionResolver> matchingBeans =applicationContext.getBeansOfType(HandlerExceptionResolver.class); 
		if (!matchingBeans.isEmpty()) {
			this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
			OrderComparator.sort(this.handlerExceptionResolvers);
		}
		//
		Map<String,WebFilter> beans=applicationContext.getBeansOfType(WebFilter.class);
		if(beans!=null && beans.size()>0){
			if(webFilters==null){
				webFilters=new ArrayList<WebFilter>(beans.size());
			}
			webFilters.addAll(beans.values());
			OrderComparator.sort(this.webFilters);
		}
		//
		Map<String, WebSAResolver> matchingBeans2 =applicationContext.getBeansOfType(WebSAResolver.class); 
		if (!matchingBeans2.isEmpty()) {
			this.webSAResolvers = new ArrayList<WebSAResolver>(matchingBeans2.values());
			OrderComparator.sort(this.webSAResolvers);
		}
	}
	
	
	
	protected List<SARContext> resolveSARs(HttpServletRequest request, HttpServletResponse response)
			throws ServletException
	{
		List<SARContext> sars=this.sarManager.listSARContext();
		if(webSAResolvers!=null && webSAResolvers.size()>0){
			for(WebSAResolver resolver:webSAResolvers){
				List<SARContext> resolvedSars=resolver.resolve(sars,request, response);
				if(resolvedSars!=null && !resolvedSars.isEmpty()){
					return resolvedSars;
				}
			}
		}
		return sars;
	}

	public void dispatchRequest(HttpServletRequest processedRequest, HttpServletResponse response) throws ServletException   {
		try {
			List<SARContext> sarList=resolveSARs(processedRequest,response);
			//
			String requestURI=urlPathHelper.getLookupPathForRequest(processedRequest);
			//
			List<WebFilter> webFilters=this.webFilters;
			EroicaWebRequestHandler handler=new EroicaWebRequestHandler(sarList,requestSARMapping,requestURI);
			if(webFilters==null || webFilters.size()==0){
				handler.handleRequest(processedRequest, response);
			}else{
				DefaultFilterChain chain=new DefaultFilterChain(webFilters,processedRequest,handler,requestURI);
				try{
					chain.doFilter(processedRequest, response);
				}catch(NestedServletException ex){
					Throwable causeEx= ex.getCause();
					throw (causeEx==null?ex:causeEx);
				}finally{
					processedRequest=chain.getProcessedRequest();
				}
			}
		}catch (Exception ex) {
			processHandlerException(processedRequest, response, null, ex);
		}catch (Throwable err) {
			ServletException ex = new NestedServletException("Handler processing failed", err);
			throw ex;
		}
	}
	
	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception ex) {
		if(handlerExceptionResolvers==null){
			return null;
		}
		ModelAndView exMv = null;
		for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
			exMv = handlerExceptionResolver.resolveException(request, response, handler, ex);
			if (exMv != null) {
				break;
			}
		}
		return null;
		//throw ex;
	}
	
	
	
	


	public List<HandlerExceptionResolver> getHandlerExceptionResolvers() {
		return handlerExceptionResolvers;
	}

	public void setHandlerExceptionResolvers(
			List<HandlerExceptionResolver> handlerExceptionResolvers) {
		this.handlerExceptionResolvers = handlerExceptionResolvers;
	}

	public SARManager getSarManager() {
		return sarManager;
	}

	public void setSarManager(SARManager sarManager) {
		this.sarManager = sarManager;
	}
	
	
}
