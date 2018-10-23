package com.sendtomoon.eroica.eoapp.context.support;

import org.springframework.beans.factory.InitializingBean;

import com.sendtomoon.eroica.common.security.PasswordProviderFactory;
import com.sendtomoon.eroica.eoapp.context.config.EoAppConfigProperties;

public class PasswordProviderConfigRefreshBean implements InitializingBean{

	private EoAppConfigProperties configProperties;

	@Override
	public void afterPropertiesSet() throws Exception {
		PasswordProviderFactory.refreshAllProviderConfig(configProperties);
	}

	public EoAppConfigProperties getConfigProperties() {
		return configProperties;
	}

	public void setConfigProperties(EoAppConfigProperties configProperties) {
		this.configProperties = configProperties;
	}
	
}
