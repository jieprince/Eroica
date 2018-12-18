package com.sendtomoon.eroica.mongodb;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.Assert;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class GridFsTemplateFactoryBean implements FactoryBean<GridFsTemplate> {
	private static final String DEFAULT_BUCKET = "filecollection";
	protected Logger logger = LoggerFactory.getLogger(GridFsTemplateFactoryBean.class);

	private MongoTemplate mongoTemplate;

	private String bucket;

	private MongoConverter converter;

	private GridFsTemplate fs;

	@Override
	public GridFsTemplate getObject() throws Exception {
		Assert.notNull(mongoTemplate, "mongoTemplate requried.");
		DB defDB = mongoTemplate.getDb();

		Mongo mongo = defDB.getMongo();
		String dbName = defDB.getName();
		MongoDbFactory dbFactory = new SimpleMongoDbFactory(mongo, dbName);
		if (converter == null) {
			converter = getDefaultMongoConverter(dbFactory);
		}
		if (StringUtils.isBlank(bucket)) {
			bucket = DEFAULT_BUCKET;
		}
		fs = new GridFsTemplate(dbFactory, converter, bucket);
		return fs;
	}

	/**
	 * this method is a clone of MongoTemplate.getDefaultMongoConverter
	 * 
	 * @param factory
	 * @return
	 */
	private final MongoConverter getDefaultMongoConverter(MongoDbFactory factory) {
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, new MongoMappingContext());
		converter.afterPropertiesSet();
		return converter;
	}

	@Override
	public Class<?> getObjectType() {
		return GridFsTemplate.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public MongoConverter getConverter() {
		return converter;
	}

	public void setConverter(MongoConverter converter) {
		this.converter = converter;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getBucket() {
		return bucket;
	}

	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

}
