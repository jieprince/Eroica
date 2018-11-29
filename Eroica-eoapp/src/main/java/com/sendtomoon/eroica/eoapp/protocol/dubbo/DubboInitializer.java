package com.sendtomoon.eroica.eoapp.protocol.dubbo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.sendtomoon.eroica.common.utils.PNetUtils;
import com.sendtomoon.eroica.common.utils.PURL;
import com.sendtomoon.eroica.pizza.PizzaConstants;
import com.sendtomoon.eroica.eoapp.EoAppException;

public class DubboInitializer implements SmartLifecycle, BeanFactoryPostProcessor {

	private static final String DUBBO_DEF_CONFIG = "META-INF/eroica/dubbo-default.properties";

	private volatile Resource configureResource;

	private volatile Properties configureProperties;

	private volatile String appName;

	protected Log logger = LogFactory.getLog(this.getClass());

	public Resource getConfigureResource() {
		return configureResource;
	}

	public void setConfigureResource(Resource configureResource) {
		this.configureResource = configureResource;
	}

	public Properties getConfigureProperties() {
		return configureProperties;
	}

	public void setConfigureProperties(Properties configureProperties) {
		this.configureProperties = configureProperties;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	private boolean isRunning;

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void start() {
		if (isRunning)
			return;
		initDubboConfigs();
	}

	@Override
	public void stop() {
		isRunning = false;
		// Dubbo协议，热部署重启不销毁，协议相关的配置变更无效
		// com.alibaba.dubbo.config.ProtocolConfig.destroyAll();
	}

	@Override
	public int getPhase() {
		return Integer.MIN_VALUE;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable runnable) {
		stop();
		runnable.run();
	}

	protected void initDubboConfigs() {
		Properties dubboProperties = new Properties();
		String pmURL = System.getProperty(PizzaConstants.KEY_MANAGER);
		if (pmURL != null && (pmURL = pmURL.trim()).length() > 0
				&& PURL.valueOf(pmURL).getProtocol().equalsIgnoreCase("zookeeper")) {
			dubboProperties.setProperty("dubbo.registry.address", pmURL);
		} else {
			dubboProperties.setProperty("dubbo.registry.address", "N/A");
		}
		dubboProperties.setProperty("dubbo.protocol.host", PNetUtils.getLocalHost());
		//

		InputStream input = this.getClass().getClassLoader().getResourceAsStream(DUBBO_DEF_CONFIG);
		try {
			dubboProperties.load(input);
		} catch (IOException e) {
			//
		} finally {
			try {
				input.close();
			} catch (IOException e) {
			}
		}
		Properties configureProperties = this.configureProperties;
		if (configureProperties == null) {
			configureProperties = new Properties();
		}
		if (configureResource != null && configureResource.exists()) {
			InputStreamReader reader = null;
			try {
				reader = new InputStreamReader(configureResource.getInputStream());
				configureProperties.load(reader);
			} catch (IOException e) {
				throw new EoAppException("Read:" + configureResource + " error,cause:" + e.getMessage(), e);
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (IOException e) {
				}
			}
		}
		if (configureProperties != null && configureProperties.size() > 0) {
			Enumeration<Object> keys = configureProperties.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				if (key.startsWith("dubbo.")) {
					dubboProperties.setProperty(key, configureProperties.getProperty(key));
				}
			}
		}
		String appName = this.appName;
		//
		if (appName == null) {
			Assert.hasLength(appName, "appName requried.");
		}
		dubboProperties.setProperty("dubbo.application.name", appName);
		//
		if (logger.isInfoEnabled()) {
			logger.info("dubboProperties=" + dubboProperties);
		}
		ConfigUtils.setProperties(dubboProperties);
		isRunning = true;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (isRunning)
			return;
		initDubboConfigs();
	}

}
