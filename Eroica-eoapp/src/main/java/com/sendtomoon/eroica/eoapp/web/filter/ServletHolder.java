package com.sendtomoon.eroica.eoapp.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.BeanUtils;

import org.springframework.beans.FatalBeanException;


public class ServletHolder  extends AbstractWebFilter{
	
	
	
	private Class<? extends Servlet> servletClazz;
	
	private Servlet servlet;
	
	private String servletName;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		servlet.service(request, response);
	}

	@Override
	public void destroy() {
		if(servlet!=null){
			servlet.destroy();
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		ServletContext servletContext=config.getServletContext();
		if(servlet==null){
			if(servletClazz==null){
				throw new FatalBeanException("filterClazz reqired");
			}
			servlet=BeanUtils.instantiate(servletClazz);
		}else{
			servletClazz=servlet.getClass();
		}
		String servletName=this.getServletName();
		if(servletName==null){
			servletName=this.getBeanName();
		}
		servlet.init(new DefaultServletConfig(servletName,this.getInitParams(),servletContext));
	}

	public String getServletName() {
		return servletName;
	}

	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	public Class<? extends Servlet> getServletClazz() {
		return servletClazz;
	}

	public void setServletClazz(Class<? extends Servlet> servletClazz) {
		this.servletClazz = servletClazz;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public void setServlet(Servlet servlet) {
		this.servlet = servlet;
	}

	

	
	
}
