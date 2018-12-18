package com.sendtomoon.eroica.mongodb;

import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.sendtomoon.eroica.common.biz.dao.DAO;

public interface MongoDAO extends DAO {

	<R> R executeInSession(DbCallback<R> action);

	MongoTemplate getMongoTemplate();

}
