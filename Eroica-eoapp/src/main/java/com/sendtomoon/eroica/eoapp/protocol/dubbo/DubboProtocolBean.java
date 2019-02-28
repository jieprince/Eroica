package com.sendtomoon.eroica.eoapp.protocol.dubbo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.ac.dubbo.EroicaAcGenericService;
import com.sendtomoon.eroica.common.exception.EroicaException;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.eoapp.protocol.ESAExporter;
import com.sendtomoon.eroica.eoapp.protocol.dubbo.ws.WebGenericService;
import com.sendtomoon.eroica.eoapp.sar.SARContext;

public class DubboProtocolBean extends ApplicationObjectSupport
		implements InitializingBean, IDubboProtocol, ESAExporter, DisposableBean, ServletContextAware {

	private Map<String, ServiceConfig<GenericService>> _configs = new ConcurrentHashMap<String, ServiceConfig<GenericService>>();

	protected Log logger = LogFactory.getLog(this.getClass());

	private ServletContext servletContext;

	public DubboProtocolBean() {
	}

	private List<RegistryConfig> defaultRegistryConfigs;

	private Map<String, RegistryConfig> registryConfigs;

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, RegistryConfig> registryConfigMap = BeanFactoryUtils
				.beansOfTypeIncludingAncestors(this.getApplicationContext(), RegistryConfig.class, false, false);
		List<RegistryConfig> defaultRegistryConfigs = new ArrayList<RegistryConfig>();
		// --------------------------------------------------------------
		if (registryConfigMap == null) {
			registryConfigMap = new LinkedHashMap<String, RegistryConfig>();
		} else {
			for (RegistryConfig config : registryConfigMap.values()) {
				if (config.isDefault() != null && config.isDefault().booleanValue()) {
					defaultRegistryConfigs.add(config);
				}
			}
		}
		//
		RegistryConfig registry = new RegistryConfig();
		registry.setDefault(true);
		defaultRegistryConfigs.add(registry);
		registryConfigMap.put("default", registry);
		//
		this.defaultRegistryConfigs = defaultRegistryConfigs;
		this.registryConfigs = registryConfigMap;
	}

	public boolean export(SARContext context, ESADefinition definition) {
		String esaName = definition.getEsaName();
		ServiceConfig<GenericService> temp = _configs.get(esaName);
		if (temp != null) {
			try {
				temp.unexport();
			} catch (Throwable th) {
				logger.error("ESA<" + esaName + "> unexport error:" + th.getMessage(), th);
			}
		}
		try {
			doExport(esaName, context, definition.getProperties());
		} catch (Throwable th) {
			throw new EroicaException("ESA<" + esaName + "> export error:" + th.getMessage(), th);
		}
		return true;
	}

	protected void doExport(String esaName, SARContext context, Properties configProperties) {
		ServiceConfig<GenericService> serviceConfig = new ServiceConfig<GenericService>();
		serviceConfig.appendProperties(configProperties);
		serviceConfig.setInterface(esaName);
		serviceConfig.setRef(createGenericService(context, configProperties));
		serviceConfig.setProvider(getProviderConfig(context.getSARName(), configProperties));

		// dubboConfig.setModule(module)
		// --------------------------
		serviceConfig.export();
		_configs.put(esaName, serviceConfig);
	}

	protected ProviderConfig getProviderConfig(String sarName, Properties configProperties) {
		ApplicationConfig aplication = new ApplicationConfig();
		aplication.setName(sarName);
		ProviderConfig providerConfig = new ProviderConfig();
		providerConfig.setApplication(aplication);
		//
		providerConfig.setRegistries(
				getRegistryConfig(StringUtils.tokenizeToStringArray(configProperties.getProperty("registry"), ",")));
		providerConfig.setProtocols(
				getProtocolConfig(StringUtils.tokenizeToStringArray(configProperties.getProperty("protocol"), ",")));
		return providerConfig;
	}

	protected GenericService createGenericService(SARContext context, Properties configProperties) {
		String mappingPath = configProperties.getProperty(ESADefinition.WEB_MAPPING_PATH);
		if (mappingPath != null && (mappingPath = mappingPath.trim()).length() > 0) {
			return new WebGenericService(context, servletContext, mappingPath);
		} else {
			return new EroicaAcGenericService(context);
		}
	}

	/*
	 * @Override public boolean export(String providerName,ESAConfig... configures)
	 * { if(configures==null || configures.length==0)return false; for(ESAConfig
	 * config:configures){ this.export(providerName,config); } return true; }
	 * 
	 * 
	 * @Override public boolean unexport(String providerName,String... esaNames) {
	 * if(esaNames==null || esaNames.length==0)return false; for(String
	 * esaName:esaNames){ this.unexport(esaName); } return true; }
	 */

	public boolean unexport(SARContext context, String esaName) {
		ServiceConfig<GenericService> temp = _configs.remove(esaName);
		if (temp != null) {
			try {
				temp.unexport();
				return true;
			} catch (Throwable th) {
				logger.error("ESA<" + esaName + "> unexport error:" + th.getMessage(), th);
				return false;
			}
		}
		return true;
	}

	public final void destroy() {
		Object[] keys = _configs.keySet().toArray();
		for (Object esaName : keys) {
			ServiceConfig<GenericService> temp = _configs.remove((String) esaName);
			if (temp != null) {
				try {
					temp.unexport();
				} catch (Throwable th) {
					logger.error("ESA<" + esaName + "> unexport error:" + th.getMessage(), th);
				}
			}
		}
		_configs.clear();
		onDestroy();
	}

	protected void onDestroy() {
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public List<RegistryConfig> getRegistryConfig(String[] registryIds) {
		if (registryIds == null || registryIds.length == 0) {
			return this.defaultRegistryConfigs;
		}
		List<RegistryConfig> configs = new ArrayList<RegistryConfig>();
		for (String id : registryIds) {
			if (id != null && (id = id.trim()).length() > 0) {
				RegistryConfig config = registryConfigs.get(id);
				if (config == null) {
					throw new NullPointerException("Not found registry<" + id + ">.");
				}
				configs.add(config);
			}
		}
		return configs;
	}

	@Override
	public List<ProtocolConfig> getProtocolConfig(String[] protocolIds) {
		if (protocolIds == null || protocolIds.length == 0) {
			return null;
		}
		List<ProtocolConfig> configs = new ArrayList<ProtocolConfig>();
		for (String id : protocolIds) {
			if (id != null && (id = id.trim()).length() > 0) {
				configs.add(new ProtocolConfig(id));
			}
		}
		return configs.size() > 0 ? configs : null;
	}

}
