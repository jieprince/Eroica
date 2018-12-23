package com.sendtomoon.eroica.eoapp.esa.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.validation.Validator;

import com.alibaba.fastjson.JSONObject;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.exception.EroicaException;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.eoapp.esa.ESADispatcher;

public class MethodESADispatcher implements ESADispatcher, ApplicationContextAware, InitializingBean {

	protected ConfigurableApplicationContext applicationContext;

	protected Log logger = LogFactory.getLog(this.getClass());

	private Map<String, MethodESA> esaMap = new HashMap<String, MethodESA>();

	private Map<String, Properties> esaConfigs;

	private List<AnnotationArgumentResolver> annotationArgumentResolvers;

	private Validator validator;

	@Override
	public final void afterPropertiesSet() throws Exception {
		if (this.getAnnotationArgumentResolvers() == null) {
			Map<String, AnnotationArgumentResolver> beans = this.getApplicationContext()
					.getBeansOfType(AnnotationArgumentResolver.class);
			if (beans != null && beans.size() > 0) {
				AnnotationArgumentResolver temp[] = new AnnotationArgumentResolver[beans.size()];
				beans.values().toArray(temp);
				OrderComparator.sort(temp);
				this.setAnnotationArgumentResolvers(Arrays.asList(temp));
			}
		}
		init();
	}

	protected void init() throws Exception {

	}

	protected MethodESA getESA(String esaName) {
		MethodESA bean = esaMap.get(esaName);
		if (bean == null) {
			throw new EroicaException("Not found esa<" + esaName + ">");
		}
		return bean;
	}

	protected void addESA(String esaName, MethodESA esa) {
		esaMap.put(esaName, esa);
	}

	protected Log getESALog(String esaName) {
		return LogFactory.getLog(esaName);
	}

	public ServiceResponse handleRequest(ServiceRequest request) {
		String esaName = request.getRequestedServiceID();
		if (esaName == null || (esaName = esaName.trim()).length() == 0) {
			throw new EroicaException("Not setter ESA name,request.requestedServiceID() is null.");
		}
		long t1 = System.nanoTime();
		try {
			MethodESA esa = getESA(esaName);
			Log esaLog = getESALog(esaName);
			if (esaLog.isDebugEnabled()) {
				esaLog.debug(
						"Handling ESA<" + esaName + "> params=" + JSONObject.toJSONString(request.getParameters()));
			}
			ServiceResponse response = esa.perform(request, this.getApplicationContext(),
					this.getAnnotationArgumentResolvers(), validator);
			Object m = (response == null ? null : response.getModel());
			if (esaLog.isDebugEnabled()) {
				esaLog.debug(
						"Handled ESA<" + esaName + ">result=" + (m == null ? "{}" : JSONObject.toJSONString(m)) + ".");
			}
			if (logger.isDebugEnabled()) {
				logger.debug(
						"#" + (System.nanoTime() - t1) / 1000 / 1000.0 + "ms# Handled ESA<" + esaName + "> completed.");
			}
			return response;
		} catch (Throwable e) {
			if (logger.isInfoEnabled()) {
				String message = "#" + (System.nanoTime() - t1) / 1000 / 1000.0 + "ms# Handled ESA<" + esaName
						+ "> failed";
				logger.info(message + ",cause:" + e.getMessage());
			}
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new EroicaException(e.getMessage(), e);
			}
		}
	}

	protected void addConfig(String esaName, Properties config) {
		if (config == null) {
			return;
		}
		if (esaConfigs == null) {
			esaConfigs = new HashMap<String, Properties>();
		}
		esaConfigs.put(esaName, config);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public Collection<ESADefinition> resolveESADefinitions() {
		if (esaMap != null && esaMap.size() > 0) {
			Set<String> esaNames = esaMap.keySet();
			ArrayList<ESADefinition> definitions = new ArrayList<ESADefinition>(esaNames.size());
			for (String esaName : esaNames) {
				if (esaName == null || esaName.length() == 0) {
					continue;
				}
				ESADefinition definition = new ESADefinition(esaName);
				if (this.esaConfigs != null) {
					definition.setProperties(esaConfigs.get(esaName));
				}
				definitions.add(definition);
			}
			return definitions;
		}
		return null;
	}

	public List<AnnotationArgumentResolver> getAnnotationArgumentResolvers() {
		return annotationArgumentResolvers;
	}

	public void setAnnotationArgumentResolvers(List<AnnotationArgumentResolver> annotationArgumentResolvers) {
		this.annotationArgumentResolvers = annotationArgumentResolvers;
	}

	public void setMethodESAList(List<MethodESA> esas) {
		for (MethodESA esa : esas) {
			this.addESA(esa.getName(), esa);
		}
	}

	public Map<String, Properties> getEsaConfigs() {
		return esaConfigs;
	}

	public void setEsaConfigs(Map<String, Properties> esaConfigs) {
		this.esaConfigs = esaConfigs;
	}

	public Validator getValidator() {
		return validator;
	}

	public void setValidator(Validator validator) {
		this.validator = validator;
	}

}
