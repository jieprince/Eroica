package com.sendtomoon.eroica.pizza;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/***
 *
 */
public abstract class BaseConfigListener extends BaseConfigLoader implements ConfigChangedListener,InitializingBean,DisposableBean {

	
	private boolean dynamicConfig=false;
	
	private boolean loadOnStatup=true;
	
	@Override
	public void afterPropertiesSet() throws Exception{
		Assert.hasText(this.getConfigKey(), "configKey required.");
		Assert.hasText(this.getConfigGroup(), "configGroup required.");
		//
		synchronized(this){
			if(this.isLoadOnStatup()){
				this.onStatupLoad(this.getConfigValue());
			}
			if(this.isDynamicConfig()){
				Pizza.setListener(this.getConfigGroup(), this.getConfigKey(), this);
			}
		}
	}
	
	

	@Override
	public void destroy() throws Exception {
		synchronized(this){
			if(this.isDynamicConfig()){
				Pizza.removeListener(this.getConfigGroup(), this.getConfigKey(), this);
			}
		}
	}


	@Override
	public void handleConfigChange(String configValue)  {
		synchronized(this){
			try {
				onChanged((String)configValue);
			} catch (Exception e) {
				logger.error("Config="+this.getFullName()+" Changed listener error,"+e.getMessage(),e);
			}
		}
	}

	protected abstract void onChanged(String configValue) ;
	

	protected abstract void onStatupLoad(String configValue) ;

	public boolean isDynamicConfig() {
		return dynamicConfig;
	}



	public void setDynamicConfig(boolean dynamicConfig) {
		this.dynamicConfig = dynamicConfig;
	}



	public boolean isLoadOnStatup() {
		return loadOnStatup;
	}



	public void setLoadOnStatup(boolean loadOnStatup) {
		this.loadOnStatup = loadOnStatup;
	}
	
	
	
	
}
