package com.sendtomoon.eroica.datasource;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.sendtomoon.eroica.common.security.PasswordContext;
import com.sendtomoon.eroica.common.security.PasswordProviderFactory;

public class DruidDataSourceFactoryBean implements FactoryBean<DataSource>, InitializingBean, DisposableBean {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private DruidDataSource dataSource;

	private Resource configureResource;

	public DruidDataSourceFactoryBean() {
	}

	@Override
	public DataSource getObject() throws Exception {
		return dataSource;
	}

	@Override
	public Class<?> getObjectType() {
		return DataSource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() throws Exception {
		try {
			if (dataSource != null) {
				dataSource.close();
			}
		} catch (Exception ex) {
			logger.error("Close datasource error:" + ex.getMessage(), ex);
		}
	}

	protected void forPassword(Properties dbConfig) {
		String password = dbConfig.getProperty("password");
		String passwordProvider = dbConfig.getProperty("passwordProvider");
		String passwordKey = dbConfig.getProperty("passwordKey");

		if (password == null && passwordProvider != null && passwordKey != null) {
			password = PasswordProviderFactory.getProvider(passwordProvider)
					.getPassword(new PasswordContext(passwordKey).setRequired(true));
		}
		if (password != null) {
			dbConfig.setProperty("password", password);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (configureResource == null) {
			throw new FatalBeanException("configure be null.");
		}
		Properties props = null;
		try (InputStream input = configureResource.getInputStream()) {
			props = new Properties();
			props.load(input);
			forPassword(props);
			dataSource = new DruidDataSource();
			dataSource.setFilters("log4j");
			DruidDataSourceFactory.config(dataSource, props);
			if (!("false".equalsIgnoreCase(props.getProperty("initialOnStartup")))) {
				dataSource.init();
			}
		} catch (Exception e) {
			throw new FatalBeanException("Create Druid datasource error by config:" + props, e);
		}
	}

	/** @deprecated */
	public Resource getConfigure() {
		return this.configureResource;
	}

	/** @deprecated */
	public void setConfigure(Resource configureResource) {
		this.configureResource = configureResource;
	}

	public void setConfigureResource(Resource configureResource) {
		this.configureResource = configureResource;
	}

}
