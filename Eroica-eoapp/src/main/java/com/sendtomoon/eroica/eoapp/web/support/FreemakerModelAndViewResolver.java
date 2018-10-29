package com.sendtomoon.eroica.eoapp.web.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.sendtomoon.eroica.common.beans.map.BeanMapUtils;
import com.sendtomoon.eroica.common.exception.ResponseCodeException;
import com.sendtomoon.eroica.common.web.WebException;

import freemarker.template.SimpleHash;
import freemarker.template.Template;

@SuppressWarnings("rawtypes")
public class FreemakerModelAndViewResolver implements HandlerMethodReturnValueHandler,Ordered
	,DisposableBean{
	
	private String defaultContentType="text/html";
	
	private Log log=LogFactory.getLog(this.getClass());
	
	private FreeMarkerConfig freeMarkerConfig;
	
	private BeanMapUtils beanMapUtils=new BeanMapUtils();
	
	

	@Override
	public boolean supportsReturnType(MethodParameter methodParameter) {
		return methodParameter.getMethod().getAnnotation(ResponseBody.class)==null;
	}


	@Override
	public void handleReturnValue(Object returnValue,
			MethodParameter methodParameter,
			ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest request) throws Exception {
		HttpServletResponse response=request.getNativeResponse(HttpServletResponse.class);
		//-----------------------------------------
		String ct=response.getContentType();
		if(ct==null){
			response.setContentType(this.getDefaultContentType());
		}
		//--------------------------------------
		StringWriter out=null;
		boolean flag=false;
		try {
			out=new StringWriter();
			ModelAndView mv = (ModelAndView)returnValue;				
			doParse(mv.getViewName(),mv.getModel(),out);
			flag=true;
		}catch (Throwable e) {
			log.error("Freemaker error:"+e.getMessage(),e);
		}
		PrintWriter pw=null;
		try {
			pw = response.getWriter();
			if(flag){
				pw.println(out.toString());
			}else{
				pw.println("{\"responseCode\":\""+ResponseCodeException.ERROR_UNKNOW+"\"}");
			}
			pw.flush();
		} catch (IOException e) {
			throw new WebException("IO error:"+e.getMessage(),e);
		}
	}
	
	
	protected void doParse(String templateName, Map model, Writer out) throws Throwable {
		Template template = null;
		template=freeMarkerConfig.getConfiguration().getTemplate(templateName);
		if (template == null) {
			throw new WebException("Not found template[" + templateName
					+ "].");
		}
		SimpleHash root = null;
		if (model instanceof Map) {
			root = new SimpleHash((Map<?,?>) model);
		} else {
			root = new SimpleHash((Map<?,?>) beanMapUtils._toMap(model));
		}
		template.process(root, out);
	}
	
	
	public FreeMarkerConfig getFreeMarkerConfig() {
		return freeMarkerConfig;
	}

	public void setFreeMarkerConfig(FreeMarkerConfig freeMarkerConfig) {
		this.freeMarkerConfig = freeMarkerConfig;
	}

	public String getDefaultContentType() {
		return defaultContentType;
	}

	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
	}

	
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}


	@Override
	public void destroy() throws Exception {
		beanMapUtils=null;
	}

	
	
}
