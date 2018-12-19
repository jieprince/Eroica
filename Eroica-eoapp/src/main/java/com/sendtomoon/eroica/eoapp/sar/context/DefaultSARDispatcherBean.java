package com.sendtomoon.eroica.eoapp.sar.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.core.OrderComparator;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationController;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.eoapp.esa.ESADispatcher;
import com.sendtomoon.eroica.eoapp.esa.exception.ESAExceptionResolver;
import com.sendtomoon.eroica.eoapp.esa.filter.DefESAFilterChain;
import com.sendtomoon.eroica.eoapp.esa.filter.ESAFilter;
import com.sendtomoon.eroica.eoapp.sar.SARDispatcher;
import com.sendtomoon.eroica.eoapp.sar.SARException;

public class DefaultSARDispatcherBean implements SARDispatcher, InitializingBean, ApplicationContextAware {

	protected Log logger = LogFactory.getLog(this.getClass());

	protected ApplicationContext applicationContext;

	private List<ESAFilter> esaFilters;

	private transient Map<String, ApplicationController> esaDispatchers;

	private transient List<ESADefinition> esaDefinitions;

	private List<ESAExceptionResolver> exceptionResolvers;

	private MessageSource messageSource;

	public ServiceResponse handleRequest(ServiceRequest request) {
		try {
			// ---------------------------------------------
			ServiceResponse response = null;
			ApplicationController dispatcher = getDispatcher(request);
			if (esaFilters != null && esaFilters.size() > 0) {
				DefESAFilterChain chain = new DefESAFilterChain(dispatcher, esaFilters);
				response = chain.doFilter(request);
				return response;
			} else {
				if (dispatcher == null) {
					throw new IllegalArgumentException(
							"Not found dispatcher for action<" + request.getRequestedServiceID() + "> .");
				}
				response = dispatcher.handleRequest(request);
			}
			MessageSource messageSource = this.getMessageSource();
			if (response != null && messageSource != null) {
				resolveMessage(messageSource, response);
			}
			return response;
		} catch (Throwable e) {
			ServiceResponse response = processHandlerException(request, e);
			if (response != null) {
				return response;
			} else {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new SARException(e.getMessage(), e);
				}
			}
		}
	}

	protected void resolveMessage(MessageSource messageSource, ServiceResponse response) {
		String responseCode = response.getResponseCode();
		if (responseCode != null && responseCode.length() > 0 && !"0".equals(responseCode)) {
			String msg = messageSource.getMessage(responseCode, null, "", Locale.CHINA);
			if (msg != null && msg.length() > 0) {
				response.setResponseMsg(msg);
			}
		}
	}

	protected ServiceResponse processHandlerException(ServiceRequest request, Throwable ex) {
		if (exceptionResolvers == null) {
			return null;
		}
		ServiceResponse resp = null;
		for (ESAExceptionResolver exceptionResolver : this.exceptionResolvers) {
			resp = exceptionResolver.resolveException(request, ex);
			if (resp != null) {
				break;
			}
		}
		return resp;
	}

	protected ApplicationController getDispatcher(ServiceRequest request) {
		String esaName = request.getRequestedServiceID();
		if (esaDispatchers != null && esaDispatchers.size() > 0) {
			return esaDispatchers.get(esaName);
		}
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		Map<String, ESAFilter> beans = this.getApplicationContext().getBeansOfType(ESAFilter.class);
		if (beans != null && beans.size() > 0) {
			if (esaFilters == null) {
				esaFilters = new ArrayList<ESAFilter>(beans.size());
			}
			esaFilters.addAll(beans.values());
			OrderComparator.sort(this.esaFilters);
		}
		Map<String, ESAExceptionResolver> matchingBeans = this.getApplicationContext()
				.getBeansOfType(ESAExceptionResolver.class);
		if (!matchingBeans.isEmpty()) {
			this.exceptionResolvers = new ArrayList<ESAExceptionResolver>(matchingBeans.values());
			OrderComparator.sort(this.exceptionResolvers);
		}
	}

	protected ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			throw new FatalBeanException("Not setter ApplicationContext or ApplicationContext initialized fail.");
		}
		return applicationContext;
	}

	public synchronized Collection<ESADefinition> getESADefinitions() {
		if (esaDispatchers == null && esaDefinitions == null) {
			esaDispatchers = new HashMap<String, ApplicationController>();
			esaDefinitions = new ArrayList<ESADefinition>();
			String beans[] = this.getApplicationContext().getBeanNamesForType(ESADispatcher.class);
			if (beans != null && beans.length > 0) {
				for (int i = 0; i < beans.length; i++) {
					ESADispatcher bean = this.getApplicationContext().getBean(beans[i], ESADispatcher.class);
					Collection<ESADefinition> definitions = bean.resolveESADefinitions();
					if (definitions != null) {
						for (ESADefinition definition : definitions) {
							esaDispatchers.put(definition.getEsaName(), bean);
							esaDefinitions.add(definition);
						}
					}
				}
			}
		}
		if (esaDefinitions == null || esaDefinitions.size() == 0)
			return null;
		return esaDefinitions;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
