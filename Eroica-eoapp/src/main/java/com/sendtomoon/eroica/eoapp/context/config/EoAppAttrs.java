package com.sendtomoon.eroica.eoapp.context.config;

import java.util.HashSet;
import java.util.Set;

import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;

public class EoAppAttrs extends EoAppConstants {

	private String appName = null;

	private Set<String> plugins = new HashSet<String>();

	public EoAppAttrs(String appName, EoAppConfigProperties configures) {
		this.appName = appName;
		String protocolsString = configures.getProperty(EoAppConstants.KEY_PROTOCOLS);
		Set<String> protocols = EroicaConfigUtils.split(protocolsString);
		if (protocols != null && protocols.contains("jetty")) {
			plugins.add("jetty");
		}
		if (configures.getProperty(KEY_HTTP_ESA_ENABLE, false)
				|| configures.getProperty(KEY_HTTP_ESA_ENABLE_EROICA, false)) {
			plugins.add("httpesa");
		}
		if (configures.getProperty(KEY_WEB_ENABLE, true)) {
			plugins.add("web");
		}
//		if (configures.getProperty(KEY_CAT_WEB_ENABLE, true)) {
//			plugins.add("catweb");
//		}
		String pluginsStr = configures.getProperty(KEY_PLUGINS);
		if (pluginsStr != null) {
			Set<String> pluginsSet = EroicaConfigUtils.split(pluginsStr);
			if (pluginsSet != null) {
				for (String plugin : pluginsSet) {
					if (plugin.startsWith("-")) {
						plugin = plugin.substring(1);
						this.plugins.remove(plugin);
					} else {
						this.plugins.add(plugin);
					}
				}
			}
		}
		// 清掉dubbo
		plugins.remove("dubbo");
	}

	public String getAppName() {
		return appName;
	}

	public Set<String> getPlugins() {
		return plugins;
	}

}
