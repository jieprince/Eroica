package com.sendtomoon.eroica.mongodb;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.Resource;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.sendtomoon.eroica.common.exception.EroicaException;
import com.sendtomoon.eroica.common.security.PasswordContext;
import com.sendtomoon.eroica.common.security.PasswordProviderFactory;
import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;

public class MongodbConfigureHandler {

	protected Log logger = LogFactory.getLog(this.getClass());

	private static final int DEFAULT_PORT = 27017;

	private static final String SERVERS_SPER = ",";

	private MongodbConfigure configure;

	private Properties configureProperties;

	private Resource configureResource;

	private String configureURL;

	protected synchronized MongodbConfigure initializeConfigure() {
		Properties configProperties = this.getConfigureProperties();
		if (this.configureResource != null) {
			if (configProperties == null) {
				configProperties = new Properties();
			}
			InputStream inputStream = null;
			try {
				configProperties.load(configureResource.getInputStream());
				if (logger.isInfoEnabled()) {
					logger.info("Load properties by resource:" + configureResource + ",properties=" + configProperties);
				}
			} catch (IOException e) {
				logger.error("Read Resource:" + configureResource + " error,cause:" + e.getMessage(), e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}
		}
		if (configureURL != null) {
			if (logger.isInfoEnabled()) {
				logger.info("configureURL=" + configureURL);
			}
			URL purl = URL.valueOf(configureURL);
			if (configProperties == null) {
				configProperties = new Properties();
			}
			configProperties.putAll(purl.getParameters());
			configProperties.put("servers", purl.getBackupAddress(DEFAULT_PORT));
		}
		MongodbConfigure configure = this.getConfigure();
		if (configProperties != null && configProperties.size() > 0) {
			if (configure == null) {
				configure = new MongodbConfigure();
				this.setConfigure(configure);
			}
			EroicaConfigUtils.bindBean(configProperties, configure);
		}
		if (configure == null) {
			throw new java.lang.IllegalArgumentException("Configure be null.");
		}
		String dbname = (configure == null ? null : configure.getDbname());
		if (dbname == null || (dbname = dbname.trim()).length() == 0) {
			throw new java.lang.IllegalArgumentException("dbname requried.");
		}
		if (logger.isInfoEnabled()) {
			logger.info("configure=" + JSONObject.toJSONString(configure));
		}
		return configure;
	}

	protected String readPassword(String user, String passwordKey, String passwordProvider) {
		if (passwordKey == null || (passwordKey = passwordKey.trim()).length() == 0) {
			passwordKey = user;
		}
		if (passwordKey == null || (passwordKey = passwordKey.trim()).length() == 0) {
			throw new EroicaException("passwordKey reqiured.");
		}

		return PasswordProviderFactory.getProvider(passwordProvider)
				.getPassword(new PasswordContext(passwordKey).setRequired(true));
	}

	protected List<MongoCredential> resolveCredentials(MongodbConfigure configure) {
		String user = configure.getUser();
		if (user == null || (user = user.trim()).length() == 0) {
			return null;
		}
		String pwd = configure.getPassword();
		if (pwd == null || (pwd = pwd.trim()).length() == 0) {
			pwd = readPassword(user, configure.getPasswordKey(), configure.getPasswordProvider());
		}
		if (MongoCredential.SCRAM_SHA_1_MECHANISM.equalsIgnoreCase(configure.getCredentialsType())) {
			return Arrays
					.asList(MongoCredential.createScramSha1Credential(user, configure.getDbname(), pwd.toCharArray()));
		} else if (MongoCredential.SCRAM_SHA_256_MECHANISM.equalsIgnoreCase(configure.getCredentialsType())) {
			return Arrays
					.asList(MongoCredential.createScramSha1Credential(user, configure.getDbname(), pwd.toCharArray()));
		} else if (MongoCredential.PLAIN_MECHANISM.equalsIgnoreCase(configure.getCredentialsType())) {
			return Arrays.asList(MongoCredential.createPlainCredential(user, configure.getDbname(), pwd.toCharArray()));
		} else if (MongoCredential.MONGODB_CR_MECHANISM.equalsIgnoreCase(configure.getCredentialsType())) {
			throw new MongoException("不能使用MONGODB-CR");
		} else {
			return null;
		}

	}

	protected List<ServerAddress> resovleServers(MongodbConfigure configure) {
		String servers = configure.getServers();
		if (servers == null || (servers = servers.trim()).length() == 0) {
			throw new java.lang.IllegalArgumentException("Servers required.");
		}
		try {
			List<ServerAddress> serverList = new ArrayList<ServerAddress>();
			for (String server : servers.split(SERVERS_SPER)) {
				URL url = URL.valueOf(server);
				serverList.add(new ServerAddress(url.getHost(), url.getPort(DEFAULT_PORT)));
			}
			return serverList;
		} catch (Exception ex) {
			throw new FatalBeanException("Config property servers=" + servers + " error, cause:" + ex.getMessage(), ex);
		}
	}

	protected MongoClientOptions resolveClientOptions(MongodbConfigure configure) {
		MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
		PropertyDescriptor[] propertyDescriptors = null;
		try {
			propertyDescriptors = Introspector.getBeanInfo(configure.getClass()).getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new FatalBeanException(e.getMessage(), e);
		}
		Class<?> builderClazz = builder.getClass();
		for (PropertyDescriptor property : propertyDescriptors) {
			String name = property.getName();
			if (name.equals("class"))
				continue;
			try {
				Object value = property.getReadMethod().invoke(configure);
				if (value != null) {
					if (name.equals("writeConcern")) {
						value = WriteConcern.valueOf(value.toString());
					}
					if (name.equals("readPreference")) {
						value = ReadPreference.valueOf(value.toString());
					}
					if (name.equals("slaveReadbale")) {
						if (value != null && value.toString().equalsIgnoreCase("true")) {
							name = "readPreference";
							value = ReadPreference.secondary();
						} else {
							continue;
						}
					}
					Method[] methods = builderClazz.getMethods();
					for (Method m : methods) {
						if (m.getName().equals(name)) {
							m.invoke(builder, value);
							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return builder.build();
	}

	public Properties getConfigureProperties() {
		return configureProperties;
	}

	public void setConfigureProperties(Properties configureProperties) {
		this.configureProperties = configureProperties;
	}

	public MongodbConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(MongodbConfigure configure) {
		this.configure = configure;
	}

	public Resource getConfigureResource() {
		return configureResource;
	}

	public void setConfigureResource(Resource configureResource) {
		this.configureResource = configureResource;
	}

	public String getConfigureURL() {
		return configureURL;
	}

	public void setConfigureURL(String configureURL) {
		this.configureURL = configureURL;
	}

}
