package com.sendtomooon.eroica.eoapp.web.filter;

import java.util.List;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.ServletContextAware;

public abstract class AbstractWebFilter implements WebFilter,Ordered
	,BeanNameAware
	,ServletContextAware{
	
	private List<String> patterns;
	
	private int order = 0;
	
	private Properties initParams;
	
	protected String beanName;
	
	protected  Log logger= LogFactory.getLog(this.getClass());
	
	private ServletContext servletContext;

	@Override
	public List<String> getPatterns() {
		return patterns;
	}

	@Override
	public  void destroy() {
		
	}
	

	@Override
	public  void setBeanName(String name) {
		if(beanName==null){
			this.beanName=name;
		}
	}
	

	@Override
	public final void setServletContext(ServletContext servletContext) {
		this.servletContext=servletContext;
	}


	@Override
	public final void afterPropertiesSet() throws Exception {
		if(patterns==null || patterns.size()==0){
			throw new FatalBeanException("patterns required.");
		}
		if(servletContext==null){
			throw new FatalBeanException("servletContext required.");
		}
		doInit(servletContext);
	}
	
	protected final void doInit(ServletContext servletContext) throws Exception{
		FilterConfig filterConfig=new DefaultFilterConfig(this.beanName,this.initParams,servletContext);
		init(filterConfig);
	}
	

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

	public void setPatterns(List<String> patterns) {
		this.patterns = patterns;
	}
	
	public void setAntPatterns(List<String> patterns) {
		this.patterns = patterns;
	}
	
	public void setPatterns(String patterns) {
		this.patterns = CollectionUtils.arrayToList(StringUtils.split(patterns, ", "));
	}
	
	public void setAntPatterns(String patterns) {
		this.patterns = CollectionUtils.arrayToList(StringUtils.split(patterns, ", "));
	}
	@SuppressWarnings("unchecked")
	public void setPattern(String pattern) {
		this.patterns = CollectionUtils.arrayToList(new String[]{pattern});
	}
	
	public void setAntPattern(String pattern) {
		setPattern(pattern);
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setInitParams(Properties initParams) {
		this.initParams = initParams;
	}

	protected String getBeanName() {
		return beanName;
	}

	public Properties getInitParams() {
		return initParams;
	}



	
	
	
	
	
}
