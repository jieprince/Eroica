package com.sendtomoon.eroica.eoapp.sar;

import java.util.Properties;
import java.util.Set;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

public class AbstractSARAttrs extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String KEY_SAR_NAME = "sar.name";

	private Properties pro;

	public AbstractSARAttrs(final Properties properties, final Properties appp) {
		this.putAll(properties);
		PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}", ":", false);
		PlaceholderResolver placeholderResolver = new PlaceholderResolver() {
			@Override
			public String resolvePlaceholder(String key) {
				String value = properties.getProperty(key);
				if (value == null && pro != null) {
					value = pro.getProperty(key);
				}
				if (value == null) {
					value = System.getProperty(key);
				}
				return value;
			}

		};
		Set<String> keys = properties.stringPropertyNames();
		for (String key : keys) {
			String value = properties.getProperty(key);
			if (value != null && value.length() > 5) {
				String newValue = helper.replacePlaceholders(value, placeholderResolver);
				if (!newValue.equals(value)) {
					super.put(key, newValue);
				}
			}
		}
		this.pro = appp;
	}

	public boolean getProperty(String key, boolean defValue) {
		String v = getProperty(key);
		return v == null ? defValue : Boolean.valueOf(v);
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public String getProperty(String key, String defValue) {
		String ret = super.getProperty(key);
		if (ret != null) {
			return ret;
		} else {
			if (this.pro != null && !key.startsWith("sar.") && (ret = pro.getProperty(key)) != null) {
				if ((ret = ret.trim()).length() > 0) {
					return ret;
				}
			}
			return defValue;
		}
	}

	public int getProperty(String key, int defValue) {
		String v = getProperty(key);
		if (v != null) {
			try {
				return Integer.parseInt(v);
			} catch (NumberFormatException ex) {
				throw new SARException("SAR property:" + key + " value:" + v + " error, required be integer.");
			}
		} else {
			return defValue;
		}
	}

}
