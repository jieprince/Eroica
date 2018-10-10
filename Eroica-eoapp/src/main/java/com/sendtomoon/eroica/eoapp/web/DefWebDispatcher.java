package com.sendtomoon.eroica.eoapp.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.UrlPathHelper;

import com.sendtomoon.eroica.eoapp.web.filter.DefaultFilterChain;
import com.sendtomoon.eroica.eoapp.web.filter.WebFilter;
import com.sendtomoon.eroica.eoapp.web.filter.impl.FileUploadFilter;

/**
 * 默认web拦截器
 * @author lbt42
 *
 */
public class DefWebDispatcher implements WebDispatcher, InitializingBean, BeanNameAware, ApplicationContextAware {

	protected Log logger = LogFactory.getLog(this.getClass());

	protected PathMatcher pathMatcher = new AntPathMatcher();

	protected UrlPathHelper urlPathHelper = new UrlPathHelper();

	private List<HandlerMapping> handlerMappings;

	private List<HandlerAdapter> handlerAdapters;

	private MultipartResolver multipartResolver;

	private List<HandlerExceptionResolver> handlerExceptionResolvers;

	private List<WebFilter> webFilters;

	private ApplicationContext applicationContext;

	private LocaleResolver localeResolver;

	private String name;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, HandlerExceptionResolver> matchingBeans = applicationContext
				.getBeansOfType(HandlerExceptionResolver.class);
		if (!matchingBeans.isEmpty()) {
			this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
			OrderComparator.sort(this.handlerExceptionResolvers);
		}
		// ---------------------
		initMultipartResolver(applicationContext);
		initLocaleResolver(applicationContext);
		if (multipartResolver != null) {
			FileUploadFilter filter = new FileUploadFilter();
			filter.setMultipartResolver(multipartResolver);
			if (webFilters == null) {
				webFilters = new ArrayList<WebFilter>();
			}
			webFilters.add(filter);
		}
		//
		Map<String, WebFilter> beans = applicationContext.getBeansOfType(WebFilter.class);
		if (beans != null && beans.size() > 0) {
			if (webFilters == null) {
				webFilters = new ArrayList<WebFilter>(beans.size());
			}
			webFilters.addAll(beans.values());
			OrderComparator.sort(this.webFilters);
		}

	}

	protected void setDefRequestAttrs(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			String requestUri = urlPathHelper.getRequestUri(request);
			logger.debug("DispatcherRequest by  '" + name + "' processing " + request.getMethod() + " request for ["
					+ requestUri + "]");
		}
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
	}

	protected boolean isMatchedByPattern(String[] patterns, String requestURI) {
		if (patterns == null || patterns.length == 0) {
			return false;
		}
		boolean matched = false;

		for (int i = 0; i < patterns.length; i++) {
			// 匹配
			String urlPattern = patterns[i];
			if (urlPattern != null && pathMatcher.match(urlPattern, requestURI)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Request:" + requestURI + " matched  by parttern:" + urlPattern);
				}
				matched = true;
				break;
			}
		}
		return matched;
	}

	public boolean handleRequest(HttpServletRequest processedRequest, HttpServletResponse response, String[] patterns)
			throws ServletException {
		//
		String requestURI = urlPathHelper.getLookupPathForRequest(processedRequest);
		boolean matchedByPattern = isMatchedByPattern(patterns, requestURI);
		if (patterns != null && patterns.length > 0 && !matchedByPattern) {
			return false;
		}
		// --------------------------------------------
		HandlerExecutionChain mappedHandler = null;
		try {
			mappedHandler = getHandler(processedRequest);
			if (!matchedByPattern && (mappedHandler == null || mappedHandler.getHandler() == null)) {
				return false;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Request:" + requestURI + " handling by:" + this.getName());
			}
			setDefRequestAttrs(processedRequest, response);
			DefWebRequestHandler handler = new DefWebRequestHandler(handlerAdapters, mappedHandler);
			if (webFilters == null || webFilters.size() == 0) {
				handler.handleRequest(processedRequest, response);
			} else {
				DefaultFilterChain chain = new DefaultFilterChain(webFilters, processedRequest, handler, requestURI);
				try {
					chain.doFilter(processedRequest, response);
				} catch (NestedServletException ex) {
					Exception causeEx = (Exception) ex.getCause();
					throw causeEx == null ? ex : causeEx;
				} finally {
					processedRequest = chain.getProcessedRequest();
				}
			}
		} catch (Exception ex) {
			Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
			processHandlerException(processedRequest, response, handler, ex);
		} catch (Error err) {
			ServletException ex = new NestedServletException("Handler processing failed", err);
			throw ex;
		}

		return matchedByPattern || (mappedHandler != null && mappedHandler.getHandler() != null);
	}

	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception ex) {
		if (handlerExceptionResolvers == null) {
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
		// throw ex;
	}

	private void initLocaleResolver(ApplicationContext context) {
		try {
			this.localeResolver = context.getBean(DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using LocaleResolver [" + this.localeResolver + "]");
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			;
			try {
				this.localeResolver = (LocaleResolver) context.getAutowireCapableBeanFactory().createBean(
						ClassUtils.forName("org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver",
								DefWebDispatcher.class.getClassLoader()));
			} catch (Exception e) {
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate LocaleResolver with name '" + DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME
						+ "': using default [" + this.localeResolver + "]");
			}
		}
	}

	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		for (HandlerMapping hm : this.handlerMappings) {
			HandlerExecutionChain handler = hm.getHandler(request);
			if (handler != null) {
				return handler;
			}
		}
		return null;
	}

	private void initMultipartResolver(ApplicationContext context) {
		try {
			this.multipartResolver = context.getBean(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME,
					MultipartResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MultipartResolver with name '"
						+ DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME + "': no multipart request handling provided");
			}
		}
	}

	public MultipartResolver getMultipartResolver() {
		return multipartResolver;
	}

	public void setMultipartResolver(MultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}

	public List<WebFilter> getWebFilters() {
		return webFilters;
	}

	public void setWebFilters(List<WebFilter> webFilters) {
		this.webFilters = webFilters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setBeanName(String beanName) {
		if (this.name == null) {
			this.name = beanName;
		}
	}

	public List<HandlerMapping> getHandlerMappings() {
		return handlerMappings;
	}

	public void setHandlerMappings(List<HandlerMapping> handlerMappings) {
		this.handlerMappings = handlerMappings;
	}

	public List<HandlerAdapter> getHandlerAdapters() {
		return handlerAdapters;
	}

	public void setHandlerAdapters(List<HandlerAdapter> handlerAdapters) {
		this.handlerAdapters = handlerAdapters;
	}

	public List<HandlerExceptionResolver> getHandlerExceptionResolvers() {
		return handlerExceptionResolvers;
	}

	public void setHandlerExceptionResolvers(List<HandlerExceptionResolver> handlerExceptionResolvers) {
		this.handlerExceptionResolvers = handlerExceptionResolvers;
	}

	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	public UrlPathHelper getUrlPathHelper() {
		return urlPathHelper;
	}

	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		this.urlPathHelper = urlPathHelper;
	}

}
