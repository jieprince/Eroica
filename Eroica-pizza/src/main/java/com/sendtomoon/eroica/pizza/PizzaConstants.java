package com.sendtomoon.eroica.pizza;

import java.nio.charset.Charset;
import java.util.Properties;

public abstract class PizzaConstants {

	/** 应用名 */
	public static final String KEY_APP_NAME = "pizza.app.name";

	/** 应用名 */
	public static final String KEY_APP_NAME_1 = "eoapp.name";

	/** 文本资源文件默认字符集 */
	/** @deprecated */
	public static final String KEY_CHARSET = "pizza.charset";

	/** 资源管理实现 */
	public static final String KEY_MANAGER = "pizza.manager";

	/** 本地备份开关,true/false */
	public static final String KEY_LOCAL_BACKUP = "pizza.local.backup";

	/** 默认pizza配置文件 */
	public static final String DEF_CONFIG_FILE = "classpath:pizza.properties";

	/** 默认pizza配置文件 property name */
	public static final String KEY_CONFIG_FILE = "pizza.config.file";

	/**
	 * 项目ID
	 * 
	 * @deprecated KEY_DOMAIN_ID
	 */
	public static final String KEY_PROJECT_ID = "pizza.project.id";

	/** 领域ID */
	public static final String KEY_DOMAIN_ID = "pizza.domain.id";

	/**
	 * 环境属性：pizza.manager
	 */
	public static final String ENV_MANAGER = "PIZZA_MANAGER";

	/**
	 * 环境属性：pizza.domain.id
	 */
	public static final String ENV_DOMAIN_ID = "PIZZA_DOMAIN_ID";

	/**
	 * 环境属性：pizza.app.name
	 */
	public static final String ENV_APP_NAME = "PIZZA_APP_NAME";

	/**
	 * 环境属性：eroica.log.home
	 */
	public static final String ENV_LOG_HOME = "PAFA_LOG_HOME";

	/***
	 * 默认Pizza资源根路径
	 */
	public static final String DEF_ROOT_PATH = "/paconfigs";

	/***
	 * Pizza资源组：组件仓库
	 */
	public static final String GROUP_LIB = "lib";

	/***
	 * Pizza资源组：应用配置文件
	 */
	public static final String GROUP_EOAPP = "eoapp";

	/***
	 * Pizza资源组：组件配置文件
	 */
	public static final String GROUP_SAR = "sar";

	/***
	 * Pizza资源组：zip文件仓库
	 */
	public static final String GROUP_ZIP = "zip";

	/***
	 * Pizza资源组：classpath资源文件组
	 */
	public static final String GROUP_RESOURCES = "resources";

	/***
	 * Pizza资源组：默认
	 */
	public static final String GROUP_DEFAULT = "def";

	/***
	 * Pizza资源组：esa
	 */
	public static final String GROUP_ESA = "esa";

	/***
	 * Pizza资源组：Redis连接池配置组
	 */
	public static final String GROUP_REDIS = "redis";

	/***
	 * Pizza资源组：DB连接池配置组
	 */
	public static final String GROUP_DATASOURCE = "datasource";

	/***
	 * Pizza资源组：mongoDB连接配置组
	 */
	public static final String GROUP_MONGODB = "mongodb";

	/***
	 * Pizza资源组：Kafka连接配置组
	 */
	public static final String GROUP_KAFKA = "kafka";

	/***
	 * Pizza资源组：TDDL配置组
	 */
	public static final String GROUP_TDDL = "patddl";

	/** Pizza资源组：全局变量 */
	public static final String GROUP_GLOBALVARS = "globalvars";

	// **** @deprecated */
	public static String getAppName() {
		String appName = getProperty(PizzaConstants.KEY_APP_NAME);
		if (appName == null) {
			appName = getProperty(PizzaConstants.KEY_APP_NAME_1);
		}
		//
		if (appName == null || (appName = appName.trim()).length() == 0) {
			_throwEx(PizzaConstants.KEY_APP_NAME + " is null.");
		}
		return appName;
	}

	// **** @deprecated */
	protected static void _throwEx(String propertyName) {
		throw new PizzaException("Not found pizza property <" + propertyName + ">.");
	}

	// **** @deprecated */
	public static boolean getBoolProperty(String key, String defValue) {
		return Boolean.valueOf(getProperty(key, defValue));
	}

	// **** @deprecated */
	public static String getProperty(String key, String defaultValue) {
		String value = System.getProperty(key, defaultValue);
		if (value == null || (value = value.trim()).length() == 0) {
			return null;
		}
		return value;
	}

	// **** @deprecated */
	public static String getProperty(String key) {
		return getProperty(key, null);
	}

	// **** @deprecated */
	public static Properties getProperties() {
		return System.getProperties();
	}

	// **** @deprecated */
	public static void setProperty(String key, String value) {
		System.setProperty(key, value);
	}

	// **** @deprecated */
	public static Charset getCharset() {
		String charset = PizzaConstants.getProperty(PizzaConstants.KEY_CHARSET, "UTF8");
		if (charset.length() > 0) {
			return Charset.forName(charset);
		} else {
			return null;
		}
	}
}
