package com.sendtomoon.eroica.mongodb;

import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class MongoTemplateFactoryBean extends MongodbConfigureHandler
		implements FactoryBean<MongoTemplate>, DisposableBean, InitializingBean {

	private boolean initializeOnStartup = true;

	private MongoClient mongoClient;

	private MongoTemplate template;

	protected MongoClient initializeMongoClient() {
		MongodbConfigure configure = this.initializeConfigure();
		MongoClientOptions clientOptions = this.resolveClientOptions(configure);
		List<ServerAddress> serverList = this.resovleServers(configure);
		if (logger.isInfoEnabled()) {
			logger.info("MongodbClient Create by configure=" + JSONObject.toJSONString(this.getConfigure()));
		}
		MongoCredential credentials = this.resolveCredentials(configure);
		if (serverList.size() == 1) {
			return new MongoClient(serverList.get(0), credentials, clientOptions);
		} else {
			return new MongoClient(serverList, credentials, clientOptions);
		}
	}

	@Override
	public MongoTemplate getObject() throws Exception {
		if (template == null) {
			this.mongoClient = this.initializeMongoClient();
			MongodbConfigure configure = this.getConfigure();
			MongoDatabase mongodb = mongoClient.getDatabase(configure.getDbname());
			if (logger.isInfoEnabled()) {
				logger.info("MongodbClient created success,stats=" + mongodb.getName());
			}
			this.template = new MongoTemplate(mongoClient, configure.getDbname());
		}
		return template;
	}

	@Override
	public Class<?> getObjectType() {
		return MongoTemplate.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() throws Exception {
		if (mongoClient != null) {
			mongoClient.close();
			mongoClient = null;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.isInitializeOnStartup()) {
			getObject();
		}
	}

	public boolean isInitializeOnStartup() {
		return initializeOnStartup;
	}

	public void setInitializeOnStartup(boolean initializeOnStartup) {
		this.initializeOnStartup = initializeOnStartup;
	}

}
