package com.sendtomoon.eroica.eoapp.esa.dispatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import com.sendtomoon.eroica.common.ac.ActionDispatcher;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerException;
import com.sendtomoon.eroica.common.app.biz.action.Action;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.ESA;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.eoapp.esa.ESADispatcher;
import com.sendtomoon.eroica.eoapp.esa.annotation.ESAConfigUtils;

/**
 * 实现了spring框架InitializingBean，实现这个bean后，在启动时，会自动调用afterPropertiesSet方法
 * 
 * @author lbt42
 *
 */
public class DefESADispatcher extends ActionDispatcher implements InitializingBean, ESADispatcher {

	private Map<String, String> mapping;

	private Map<String, Properties> esaConfigs;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (mapping == null) {
			this.mapping = getMapping(this.getApplicationContext());
			;
		}
	}

	@Override
	public ServiceResponse handleRequest(ServiceRequest request) throws ApplicationControllerException {
		if (mapping != null && mapping.size() > 0) {
			String esaName = request.getRequestedServiceID();
			String beanName = mapping.get(esaName);
			if (beanName != null) {
				request.setRequestedServiceID(beanName);
			}
		}
		ServiceResponse response = super.handleRequest(request);
		return response;
	}

	@Override
	public Collection<ESADefinition> resolveESADefinitions() {
		if (mapping != null && mapping.size() > 0) {
			Set<String> esaNames = mapping.keySet();
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

	protected void addConfig(String esaName, Properties config) {
		if (config == null) {
			return;
		}
		if (esaConfigs == null) {
			esaConfigs = new HashMap<String, Properties>();
		}
		esaConfigs.put(esaName, config);
	}

	protected Map<String, String> getMapping(ApplicationContext applicationContext) {
		Map<String, String> esaMap = new HashMap<String, String>();
		Map<String, Action> actions = applicationContext.getBeansOfType(Action.class);
		if (actions != null && actions.size() > 0) {
			for (Map.Entry<String, Action> e : actions.entrySet()) {
				String beanName = e.getKey();
				Object bean = e.getValue();
				Class<?> beanClazz = AopUtils.getTargetClass(bean);
				ESA annotation = beanClazz.getAnnotation(ESA.class);
				String esaName = null;
//				if (annotation == null) {
//					if (bean instanceof ESA) {
//						esaName = beanName;
//					}
//				} else {
				esaName = annotation.value();
				if (esaName.length() == 0) {
					esaName = annotation.name();
				}
				if (esaName.length() == 0) {
					esaName = beanName;
				}
//				}
				if (esaName != null) {
					if (annotation != null) {
						this.addConfig(esaName, ESAConfigUtils.getESAConfig(annotation));
					}
					esaMap.put(esaName, beanName);
				}
			}
		}
		return esaMap;
	}

	public Map<String, Properties> getEsaConfigs() {
		return esaConfigs;
	}

	public void setEsaConfigs(Map<String, Properties> esaConfigs) {
		this.esaConfigs = esaConfigs;
	}

}
