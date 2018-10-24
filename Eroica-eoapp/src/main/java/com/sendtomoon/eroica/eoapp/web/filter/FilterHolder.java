package com.sendtomoon.eroica.eoapp.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.BeanUtils;

import org.springframework.beans.FatalBeanException;


public class FilterHolder  extends AbstractWebFilter{
	
	private Class<? extends Filter> filterClazz;
	
	private Filter filter;
	


	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		filter.doFilter(request, response, chain);
	}

	@Override
	public void destroy() {
		if(filter!=null){
			filter.destroy();
		}
	}

	
	@Override
	public void init(FilterConfig config) throws ServletException {
		if(filter==null){
			if(filterClazz==null){
				throw new FatalBeanException("filterClazz reqired");
			}
			filter=BeanUtils.instantiate(filterClazz);
		}else{
			filterClazz=filter.getClass();
		}
		filter.init(config);
	}

	public Class<? extends Filter> getFilterClazz() {
		return filterClazz;
	}

	public void setFilterClazz(Class<? extends Filter> filterClazz) {
		this.filterClazz = filterClazz;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public String getFilterName(){
		return this.getBeanName();
	}
	
	public void setFilterName(String filterName){
		this.setBeanName(filterName);
	}
	
}
