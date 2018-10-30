package com.sendtomoon.eroica.eoapp.sar.context;

import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.sendtomoon.eroica.eoapp.sar.SARContextAttrs;

public class SARPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private SARContextAttrs attrs;

	@Override
	public void setProperties(Properties properties) {
		this.attrs=(SARContextAttrs)properties;
		super.setProperties(properties);
	}


	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
		return attrs.getProperty(placeholder);
	}

	
}
