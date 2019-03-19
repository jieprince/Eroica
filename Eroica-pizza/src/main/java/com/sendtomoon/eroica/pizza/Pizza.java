package com.sendtomoon.eroica.pizza;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ResourceUtils;

import com.sendtomoon.eroica.common.Eroica;
import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;
import com.sendtomoon.eroica.pizza.context.RootContextBean;
import com.sendtomoon.eroica.pizza.log4j.DefaultLogUtils;
import com.sendtomoon.eroica.pizza.spring.PizzaResourceListener;

/**
 * 读取配置文件
 * 
 */
public class Pizza extends PizzaConstants {

	static volatile PizzaContext pizzaContext;

	private volatile static ConfigurableApplicationContext context = null;

	private volatile static Log startupLogger;

	static {
		long t1 = System.nanoTime();
		startupLogger = DefaultLogUtils.getLogger();
		try {
			MDCUtil.set();

			loadConfigResource();
			context = new RootContextBean();
			pizzaContext = context.getBean(PizzaContext.class);
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					System.out.println("Eroica<" + Pizza.getAppName() + "> shutdown hook now" + ",datetime="
							+ DateFormat.getDateTimeInstance().format(new Date()) + ".");
					if (context != null)
						context.close();
					com.alibaba.dubbo.config.ProtocolConfig.destroyAll();
				}
			}, "EroicaShutdownHook"));
		} catch (Exception ex) {
			String msg = "Pizza initialized error,cause:" + ex.getMessage();
			ex.printStackTrace(System.err);
			startupLogger.error(msg, ex);
		} finally {
			if (startupLogger.isInfoEnabled()) {
				startupLogger.info("PizzaStartuped,times=" + (System.nanoTime() - t1) / 1000 / 1000.0 + "ms.");
			}
		}
	}

	public static ConfigurableApplicationContext getSpringContext() {
		return context;
	}

	public static String getAppName() {
		return pizzaContext.getAppName();
	}

	/***
	 * @deprecated getDomainId
	 * @return
	 */
	public static String getProjectId() {
		return pizzaContext.getProjectId();
	}

	public static String getDomainId() {
		return pizzaContext.getDomainId();
	}

	public static String get(String group, String key) {
		return pizzaContext.get(group, key);
	}

	public static boolean exists(String group, String key) {
		return pizzaContext.exists(group, key);
	}

	public static boolean registerListener(PizzaResourceListener listener) {
		return pizzaContext.registerListener(listener);
	}

	public static boolean unregisterListener(PizzaURL pizzaURL) {
		return pizzaContext.unregisterListener(pizzaURL);
	}

	public static Charset getCharset() {
		return pizzaContext.getCharset();
	}

	/**
	 * @deprecated
	 * @param group
	 * @param key
	 * @param listener
	 */
	public static void setListener(String group, String key, ConfigChangedListener listener) {
		pizzaContext.getDefaultManager().setListener(group, key, listener);
	}

	/***
	 * @deprecated
	 * @param group
	 * @param key
	 * @param listener
	 */
	public static void removeListener(String group, String key, ConfigChangedListener listener) {
		pizzaContext.getDefaultManager().removeListener(group, key, listener);
	}

	public static PizzaManager getManager() {
		return pizzaContext.getDefaultManager();
	}

	public static PizzaContext getPizzaContext() {
		return pizzaContext;
	}

	private static synchronized void loadConfigResource() throws PizzaException {
		String configResource = null;
		try {
			configResource = System.getProperty(PizzaConstants.KEY_CONFIG_FILE);
			InputStream resourceInput = null;
			if (configResource == null || (configResource = configResource.trim()).length() == 0) {
				configResource = PizzaConstants.DEF_CONFIG_FILE;

				try {
					resourceInput = ResourceUtils.getURL(configResource).openStream();
				} catch (FileNotFoundException ex) {
				}
			} else {
				resourceInput = ResourceUtils.getURL(configResource).openStream();
				if (resourceInput == null) {
					throw new FileNotFoundException("Resource=" + configResource + " not found.");
				}
			}

			if (resourceInput == null) {
				startupLogger.info("Pizza configure resource not found.");
				loadSystemEnvs();
				return;
			}

			Properties properties = new Properties();
			startupLogger.info("Found Pizza configure resource=" + configResource);

			try {
				properties.load(new InputStreamReader(resourceInput, "UTF-8"));
			} finally {
				resourceInput.close();
			}

			for (Object key : properties.keySet()) {
				String value = properties.getProperty((String) key, "");
				if (value != null && value.length() > 0) {
					System.setProperty((String) key, value);
				}
			}

			startupLogger.info("Parsed Pizza configure resource=" + configResource + ",propertiesSize="
					+ properties.size() + (properties.size() > 0 ? ",properties:" + properties : ""));
		} catch (Throwable th) {
			throw new PizzaException("Loaded Pizza configure failure,cause:" + th.getMessage(), th);
		}
	}

	/**
	 * 从系统环境变量设置Pafa5变量值
	 */
	private static void loadSystemEnvs() {
		String pizzaManager = System.getenv(PizzaConstants.ENV_MANAGER);
		if (StringUtils.isNotBlank(pizzaManager)) {
			System.setProperty(PizzaConstants.KEY_MANAGER, pizzaManager);
		}
		String pizzaDomainId = System.getenv(PizzaConstants.ENV_DOMAIN_ID);
		if (StringUtils.isNotBlank(pizzaDomainId)) {
			System.setProperty(PizzaConstants.KEY_DOMAIN_ID, pizzaDomainId);
		}
		String pizzaAppName = System.getenv(PizzaConstants.ENV_APP_NAME);
		if (StringUtils.isNotBlank(pizzaAppName)) {
			System.setProperty(PizzaConstants.KEY_APP_NAME, pizzaAppName);
		}
		String pafaLogHome = System.getenv(PizzaConstants.ENV_LOG_HOME);
		if (StringUtils.isNotBlank(pafaLogHome)) {
			System.setProperty(Eroica.LOG_HOME_KEY, pafaLogHome);
		}
	}

}
