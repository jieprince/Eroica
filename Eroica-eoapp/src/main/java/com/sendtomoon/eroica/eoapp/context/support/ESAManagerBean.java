package com.sendtomoon.eroica.eoapp.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerLocal;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.utils.ESAPatternUtils;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.eoapp.esa.exception.ESAExceptionResolver;
import com.sendtomoon.eroica.eoapp.esa.filter.DefESAFilterChain;
import com.sendtomoon.eroica.eoapp.esa.filter.ESAFilter;
import com.sendtomoon.eroica.pizza.Pizza;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;
import com.sendtomoon.eroica.pizza.utils.PizzaProperties;
import com.sendtomoon.eroica.eoapp.EoAppException;
import com.sendtomoon.eroica.eoapp.protocol.ESAExporter;
import com.sendtomoon.eroica.eoapp.sar.SARContext;

public class ESAManagerBean implements ESAManager, InitializingBean, ApplicationContextAware {

	protected Log logger = LogFactory.getLog(this.getClass());

	private volatile Map<String, SARContext> esaMaps;

	private volatile List<ESAExporter> exporters;

	private volatile List<ESAFilter> esaFilters;

	private volatile List<ESAExceptionResolver> exceptionResolvers;

	public ESAManagerBean() {
		esaMaps = new ConcurrentHashMap<String, SARContext>();
	}

	public ESAManagerBean(List<ESAExporter> exporters, List<ESAFilter> esaFilters,
			List<ESAExceptionResolver> exceptionResolvers) {
		esaMaps = new ConcurrentHashMap<String, SARContext>();
		this.exporters = exporters;
		this.esaFilters = esaFilters;
		this.exceptionResolvers = exceptionResolvers;
	}

	private volatile ConfigurableApplicationContext applicationContext = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	protected void forESAFilter(ConfigurableApplicationContext applicationContext) {
		Map<String, ESAFilter> beans = applicationContext.getBeansOfType(ESAFilter.class);
		if (beans != null && beans.size() > 0) {
			if (esaFilters == null) {
				esaFilters = new ArrayList<ESAFilter>(beans.size());
			}
			esaFilters.addAll(beans.values());
			OrderComparator.sort(this.esaFilters);
		}
	}

	protected void forExporters(ConfigurableApplicationContext applicationContext) {
		Map<String, ESAExporter> matchingBeans = applicationContext.getBeansOfType(ESAExporter.class);
		if (!matchingBeans.isEmpty()) {
			this.exporters = new ArrayList<ESAExporter>(matchingBeans.values());
			OrderComparator.sort(this.exporters);
		}
	}

	protected void forExceptionResolvers(ConfigurableApplicationContext applicationContext) {
		Map<String, ESAExceptionResolver> matchingBeans = applicationContext.getBeansOfType(ESAExceptionResolver.class);
		if (!matchingBeans.isEmpty()) {
			this.exceptionResolvers = new ArrayList<ESAExceptionResolver>(matchingBeans.values());
			OrderComparator.sort(this.exceptionResolvers);
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		forESAFilter(applicationContext);
		forExporters(applicationContext);
		forExceptionResolvers(applicationContext);
	}

	@Override
	public void unexportAll() {
		if (logger.isInfoEnabled()) {
			logger.info("Do unexportAll ESA...");
		}
		Map<String, SARContext> esaMaps = this.esaMaps;
		if (esaMaps == null) {
			return;
		}
		String[] keys = new String[esaMaps.size()];
		esaMaps.keySet().toArray(keys);
		for (String key : keys) {
			this.unexport(key);
		}
		if (logger.isInfoEnabled()) {
			logger.info("Do unexportAll ESA completed.");
		}
	}

	@Override
	public boolean unexport(String esaName) {
		boolean flag = false;
		SARContext context = null;
		if ((context = esaMaps.remove(esaName)) != null) {
			flag = true;
			if (logger.isInfoEnabled()) {
				logger.info("UnexportESA<" + esaName + "> by SAR<" + context.getSARName() + ">.");
			}
			if (exporters != null && !exporters.isEmpty()) {
				for (ESAExporter exporter : exporters) {
					flag = exporter.unexport(context, esaName);
				}
			}
		}
		return flag;
	}

	@Override
	public boolean isExported(String esaName) {
		return esaMaps.containsKey(esaName);
	}

	@Override
	public void export(SARContext context) {
		Collection<ESADefinition> definitions = context.getESADefinitions();
		if (definitions != null && definitions.size() > 0) {
			for (ESADefinition definition : definitions) {
				this.export(context, definition);
			}
		}
	}

	@Override
	public void unexport(SARContext context) {
		Collection<ESADefinition> definitions = context.getESADefinitions();
		if (definitions != null && definitions.size() > 0) {
			for (ESADefinition definition : definitions) {
				String esaName = definition.getEsaName();
				//
				try {
					this.unexport(esaName);
				} catch (Exception e) {
					logger.error(
							"SAR<" + context.getSARName() + ">unexport esa error,cause:\n" + e.getLocalizedMessage(),
							e);
				}
			}
		}
	}

	public boolean export(SARContext sar, ESADefinition definition) {
		String esaName = definition.getEsaName();
		ESAPatternUtils.check(esaName);
		if (this.isExported(esaName)) {
			throw new EoAppException("ESA<" + esaName + "> export repeat.");
		}
		if (exporters != null && !exporters.isEmpty()) {
			Properties properties = definition.getProperties();
			if (properties == null) {
				properties = new Properties();
			}
			PizzaProperties.loadProperties(properties,
					PizzaURL.valueOf(Pizza.GROUP_ESA + "/" + esaName + ".properties"));
			if (logger.isDebugEnabled()) {
				logger.info("ESA<" + esaName + "> configure=" + properties);
			}
			definition.setProperties(properties);
			if (!definition.isLocal()) {
				for (ESAExporter exporter : exporters) {
					exporter.export(sar, definition);
				}
			}
		}
		esaMaps.put(esaName, sar);
		if (logger.isInfoEnabled()) {
			logger.info("ExportESA<" + esaName + "> by SAR<" + sar.getSARName() + ">...");
		}
		return true;
	}

	@Override
	public ServiceResponse handleRequest(ServiceRequest request) {
		return handleRequest(request, true);
	}

	@Override
	public ServiceResponse handleRequest(ServiceRequest request, boolean includeFilters) {
		String esaName = request.getRequestedServiceID();
		try {
			if (includeFilters && esaFilters != null && esaFilters.size() > 0) {
				SARContext sar = esaMaps.get(esaName);
				ApplicationControllerLocal dispatcher = sar == null ? null : sar;
				DefESAFilterChain chain = new DefESAFilterChain(dispatcher, esaFilters);
				ServiceResponse response = chain.doFilter(request);
				return response;
			} else {
				SARContext sar = esaMaps.get(esaName);
				if (sar == null) {
					throw new EoAppException("ESA:" + esaName + " Not found .");
				}
				return sar.handleRequest(request);
			}
		} catch (Throwable e) {
			ServiceResponse response = processHandlerException(request, e);
			if (response != null) {
				return response;
			} else {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new EoAppException(e.getMessage(), e);
				}
			}
		}
	}

	protected ServiceResponse processHandlerException(ServiceRequest request, Throwable ex) {
		if (exceptionResolvers == null) {
			return null;
		}
		ServiceResponse resp = null;
		for (ESAExceptionResolver exceptionResolver : exceptionResolvers) {
			resp = exceptionResolver.resolveException(request, ex);
			if (resp != null) {
				break;
			}
		}
		return resp;
	}

}
