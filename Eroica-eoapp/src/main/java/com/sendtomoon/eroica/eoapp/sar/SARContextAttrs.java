package com.sendtomoon.eroica.eoapp.sar;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;

public class SARContextAttrs extends AbstractSARAttrs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 基包
	protected final static String KEY_BASE_PACKAGE = "sar.base.package";

	/** @deprecated */
	// 是否支持web请求
	protected final static String KEY_WEB_ENABLE = "sar.web.enable";

	private final static String KEY_PLUGINS = "sar.plugins";
	//
	protected final static String KEY_WEB_PATTERNS = "sar.web.patterns";

	protected final static String KEY_DEF_CHARSET = "sar.def.charset";

	// 处理web请求的优先级
	protected final static String KEY_ORDER = "sar.web.order";

	/** @deprecated */
	// 是否启用ibatis
	private final static String KEY_ENABLE_IBATIS = "sar.ibatis.enable";
	/** @deprecated */
	// 是否启用quartz
	private final static String KEY_ENABLE_QUARTZ = "sar.quartz.enable";

	/** @deprecated */
	// 引用数据源KEY
	private final static String KEY_DATASOURCE_KEY = "sar.datasource.key";

	// 引用数据源configURL
	protected final static String KEY_DATASOURCE = "sar.datasource";

	/** @deprecated */
	// 是否启用DB事务支持
	protected final static String KEY_TRANSACTION_ENABLE = "sar.transaction.enable";

	private volatile String sarName = null;

	private volatile String[] basePackages;

	private volatile String[] webPatterns;

	private volatile String defCharset;

	private volatile Set<String> plugins;

	private volatile Class<?> bootClass;

	private volatile int order;

	SARContextAttrs(String sarName, Properties properties, Class<?> bootClass, Set<String> plugins,
			Properties eoappProperties) {
		super(properties, eoappProperties);
		this.sarName = sarName;
		this.bootClass = bootClass;
		//
		this.plugins = plugins;

		plugins.add("consumer");
		//
		if (getProperty(KEY_WEB_ENABLE, false)) {
			plugins.add("web");
		}
		;
		String webPatterns = getProperty(KEY_WEB_PATTERNS);
		if (webPatterns != null && webPatterns.length() > 0) {
			Set<String> webPatternSet = EroicaConfigUtils.split(webPatterns);
			if (webPatternSet != null) {
				this.webPatterns = new String[webPatternSet.size()];
				webPatternSet.toArray(this.webPatterns);
			}
		}
		String pluginsStr = getProperty(KEY_PLUGINS);
		if (pluginsStr != null) {
			Set<String> pluginsSet = EroicaConfigUtils.split(pluginsStr);
			if (pluginsSet != null) {
				this.plugins.addAll(pluginsSet);
			}
		}
		// ---------------------------------------------------
		this.defCharset = getProperty(KEY_DEF_CHARSET);
		if (defCharset != null) {
			try {
				Charset.forName(defCharset);
			} catch (Exception ex) {
				throw new IllegalArgumentException(
						"Property<" + KEY_DEF_CHARSET + ">=" + defCharset + "  error,cause:" + ex.getMessage());
			}
		}
		if (getProperty(KEY_ENABLE_IBATIS, false)) {
			plugins.add("ibatis");
		}
		if (getProperty(KEY_ENABLE_QUARTZ, false)) {
			plugins.add("quartz");
		}
		this.order = getProperty(KEY_ORDER, 0);
		//
		String datasourceConfigure = getProperty(KEY_DATASOURCE_KEY);
		if (datasourceConfigure != null && datasourceConfigure.length() > 0) {
			this.setProperty(KEY_DATASOURCE, datasourceConfigure);
		} else {
			datasourceConfigure = getProperty(KEY_DATASOURCE);
		}
		if (datasourceConfigure != null && datasourceConfigure.length() > 0) {
			if (datasourceConfigure.charAt(0) != '/') {
				datasourceConfigure = "/" + datasourceConfigure;
			}
			plugins.add("datasource");
		}
		if (getProperty(KEY_TRANSACTION_ENABLE, false)) {
			plugins.add("transaction");
		}
		String basePackage = getProperty(KEY_BASE_PACKAGE);
		if (basePackage != null && basePackage.length() > 0) {
			String[] basePackageArray = StringUtils.split(basePackage, ",");
			List<String> basePackageList = new ArrayList<String>();
			for (String bpStr : basePackageArray) {
				if (bpStr != null && (bpStr = bpStr.trim()).length() > 0) {
					basePackageList.add(bpStr);
				}
			}
			if (basePackageList != null && basePackageList.size() > 0) {
				String[] basePackages = new String[basePackageList.size()];
				basePackageList.toArray(basePackages);
				this.basePackages = basePackages;
			}
		} else if (this.bootClass != null && this.bootClass.getPackage() != null) {
			String pgName = this.bootClass.getPackage().getName();
			if (pgName != null && pgName.length() > 0)
				this.basePackages = new String[] { pgName };
		}
		Object[] pluginsArrays = plugins.toArray();
		for (Object pluginName : pluginsArrays) {
			String name = (String) pluginName;
			if (name.startsWith("-")) {
				plugins.remove(name);
				plugins.remove(name.substring(1));
			}
		}
	}

	public String getSarName() {
		return sarName;
	}

	public int getOrder() {
		return order;
	}

	public String[] getWebPatterns() {
		return webPatterns;
	}

	public Set<String> getPlugins() {
		return plugins;
	}

	public String[] getBasePackages() {
		return basePackages;
	}

	public Class<?> getBootClass() {
		return bootClass;
	}

}
