package com.sendtomoon.eroica.eoapp.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class DefWebRequestHandler implements WebRequestHandler{
	
	
	private HandlerExecutionChain mappedHandler;
	
	private static Log logger=LogFactory.getLog(DefWebRequestHandler.class);
	
	private List<HandlerAdapter> handlerAdapters;
	
	
	public  DefWebRequestHandler(List<HandlerAdapter> handlerAdapters
			,HandlerExecutionChain mappedHandler) {
		this.mappedHandler=mappedHandler;
		this.handlerAdapters=handlerAdapters;
	}

	protected static final Log pageNotFoundLogger = LogFactory.getLog("WebNotFoundLogger");
	
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response)  {
		pageNotFoundLogger.error("No mapping found for HTTP request with URI [" + request.getRequestURI() +
				"].");
		try {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			
		}
	}

	@Override
	public void handleRequest(HttpServletRequest processedRequest, HttpServletResponse response) throws Exception{
		if(mappedHandler==null || mappedHandler.getHandler()==null){
			noHandlerFound(processedRequest,response);
			return ;
		}
		
		Exception err=null;
		int interceptorIndex = -1;
		try{
			ModelAndView mv=null;
			// Determine handler adapter for the current request.
			HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

			// Apply preHandle methods of registered interceptors.
			HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
			if (interceptors != null) {
				for (int i = 0; i < interceptors.length; i++) {
					HandlerInterceptor interceptor = interceptors[i];
					if (!interceptor.preHandle(processedRequest, response, mappedHandler.getHandler())) {
						return ;
					}
					interceptorIndex = i;
				}
			}
			// Actually invoke the handler.
			mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
			// Apply postHandle methods of registered interceptors.
			if (interceptors != null) {
				for (int i = interceptors.length - 1; i >= 0; i--) {
					HandlerInterceptor interceptor = interceptors[i];
					interceptor.postHandle(processedRequest, response, mappedHandler.getHandler(), mv);
				}
			}
		}catch(Exception ex ){
			err=ex;
			throw ex;
		}finally{
			triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, err);
		}
	}
	
	

	

	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		for (HandlerAdapter ha : this.handlerAdapters) {
			if (ha.supports(handler)) {
				return ha;
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: Does your handler implement a supported interface like Controller?");
	}

	
	private void triggerAfterCompletion(HandlerExecutionChain mappedHandler,
			int interceptorIndex,
			HttpServletRequest request,
			HttpServletResponse response,
			Exception ex) throws Exception {

		// Apply afterCompletion methods of registered interceptors.
		if (mappedHandler != null) {
			HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
			if (interceptors != null) {
				for (int i = interceptorIndex; i >= 0; i--) {
					HandlerInterceptor interceptor = interceptors[i];
					try {
						interceptor.afterCompletion(request, response, mappedHandler.getHandler(), ex);
					}
					catch (Throwable ex2) {
						logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
					}
				}
			}
		}
	}


	
	
}
