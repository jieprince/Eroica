package com.sendtomoon.eroica.mongodb.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

public class BaseMongoDAO<T> extends EntityObjectHandler<T> implements MongoDAO {

	private MongoTemplate _mongoTemplate;

	private String collectionName;

	protected void _add(T... entities) {
		if (entities == null || entities.length == 0) {
			return;
		}
		this.getMongoTemplate().insert(Arrays.asList(entities), getCollectionName());
	}

	protected void _add(Collection<T> entities) {
		if (entities == null || entities.size() == 0) {
			return;
		}
		this.getMongoTemplate().insert(entities, getCollectionName());
	}

	protected void _add(T entity) {
		this.getMongoTemplate().insert(entity, getCollectionName());
	}

	protected void _save(T entity) {
		this.getMongoTemplate().save(entity, getCollectionName());
	}

	protected int _updateMulti(Criteria criteria, T entity) {
		Update update = null;
		if (entity != null) {
			DBObject obj = (DBObject) this.getMongoTemplate().getConverter().convertToMongoType(entity);
			update = Update.fromDBObject(obj);
		}
		return _updateMulti(criteria, update);
	}

	protected int _updateMulti(Criteria criteria, Update update) {
		if (logger.isDebugEnabled()) {
			logger.debug("UpdateMulti,criteria=" + (criteria == null ? null : criteria.getCriteriaObject())
					+ "\nUpdate=" + (update == null ? null : update.toString()));
		}

		WriteResult result = this.getMongoTemplate().updateMulti(criteria == null ? null : Query.query(criteria),
				update, this.getEntityClass(), this.getCollectionName());
		int count = result.getN();
		if (logger.isDebugEnabled()) {
			logger.debug("UpdateMulti,result=" + result);
		}
		return count;
	}

	protected int _upsert(Criteria criteria, T entity) {
		Update update = null;
		if (entity != null) {
			DBObject obj = (DBObject) this.getMongoTemplate().getConverter().convertToMongoType(entity);
			update = Update.fromDBObject(obj);
		}
		return _upsert(criteria, update);
	}

	protected int _upsert(Criteria criteria, Update update) {
		if (logger.isDebugEnabled()) {
			logger.debug("Upsert,criteria=" + (criteria == null ? null : criteria.getCriteriaObject()) + "\nUpdate="
					+ (update == null ? null : update.toString()));
		}

		WriteResult result = this.getMongoTemplate().upsert(criteria == null ? null : Query.query(criteria), update,
				this.getEntityClass(), this.getCollectionName());
		int count = result.getN();
		if (logger.isDebugEnabled()) {
			logger.debug("Upsert,result=" + result);
		}
		return count;
	}

	public <R> R executeInSession(DbCallback<R> action) {
		return this.getMongoTemplate().executeInSession(action);
	}

	protected boolean _exists(Criteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Exists,criteria=" + (criteria == null ? null : criteria.getCriteriaObject()));
		}
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		}
		return this.getMongoTemplate().exists(query, this.getEntityClass(), this.getCollectionName());
	}

	protected List<T> _list(Criteria criteria) {
		return _list(criteria, -1, -1, null);
	}

	protected List<T> _list(Criteria criteria, int skip, int limitSize) {
		return _list(criteria, skip, limitSize, null);
	}

	protected List<T> _listAndDesc(Criteria criteria, int skip, int limitSize, String... orderBy) {
		return _list(criteria, skip, limitSize, (orderBy == null ? null : new Sort(Direction.DESC, orderBy)));
	}

	protected List<T> _listAndAsc(Criteria criteria, int skip, int limitSize, String... orderBy) {
		return _list(criteria, skip, limitSize, (orderBy == null ? null : new Sort(Direction.ASC, orderBy)));
	}

	protected List<T> _listAndDesc(Criteria criteria, String... orderBy) {
		return _list(criteria, -1, -1, (orderBy == null ? null : new Sort(Direction.DESC, orderBy)));
	}

	protected List<T> _listAndAsc(Criteria criteria, String... orderBy) {
		return _list(criteria, -1, -1, (orderBy == null ? null : new Sort(Direction.ASC, orderBy)));
	}

	protected List<T> _list(Criteria criteria, int skip, int limitSize, Sort sort) {
		if (logger.isDebugEnabled()) {
			logger.debug("List,skip=" + skip + ",limitSize=" + limitSize + ",Criteria="
					+ (criteria == null ? null : criteria.getCriteriaObject()) + ",Sort=" + sort);
		}
		Query query = new Query();
		if (criteria != null) {
			query.addCriteria(criteria);
		}
		if (sort != null) {
			query.with(sort);
		}
		if (skip > 0)
			query.skip(skip);
		if (limitSize > 0)
			query.limit(limitSize);
		return this.getMongoTemplate().find(query, this.getEntityClass(), getCollectionName());
	}

	protected List<T> _listQuery(Query query) {
		return this.getMongoTemplate().find(query, this.getEntityClass(), getCollectionName());
	}

	protected void _listQuery(Query query, DocumentCallbackHandler dch) {
		this.getMongoTemplate().executeQuery(query, this.getCollectionName(), dch);
	}

	protected boolean _updateById(Object id, T entity) {
		String idPropertyName = this.getIdPropertyName();
		if (id == null) {
			throw new MongoException("id is null.");
		}
		return _update(Criteria.where(idPropertyName).is(id), entity);
	}

	protected boolean _updateById(T entity) {
		String idPropertyName = this.getIdPropertyName();
		Object idValue = getIdValue(entity);
		return _update(Criteria.where(idPropertyName).is(idValue), entity);
	}

	protected boolean _updateByProperty(String whereProperty, Object whereValue, T dto) {
		if (whereProperty == null || whereValue == null) {
			throw new MongoException("Parameter is null.");
		}
		return _update(Criteria.where(whereProperty).is(whereValue), dto);
	}

	protected boolean _update(Criteria criteria, Update update) {
		return _updateMulti(criteria, update) > 0;
	}

	protected boolean _updateFirst(Criteria criteria, Update update) {
		if (logger.isDebugEnabled()) {
			logger.debug("UpdateFirst,Criteria=" + (criteria == null ? null : criteria.getCriteriaObject())
					+ "\nUpdate=" + (update == null ? null : update.toString()));
		}
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		}
		WriteResult result = this.getMongoTemplate().updateFirst(query, update, this.getEntityClass(),
				this.getCollectionName());
		if (logger.isDebugEnabled()) {
			logger.debug("UpdateFirst,result=" + result);
		}
		return result.getN() == 1;
	}

	protected T _getAndUpdate(Criteria criteria, T entity) {
		if (entity == null) {
			throw new MongoException("entity is null");
		}
		DBObject obj = (DBObject) this.getMongoTemplate().getConverter().convertToMongoType(entity);
		return _getAndUpdate(criteria, Update.fromDBObject(obj));
	}

	protected T _getAndRemove(Criteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("GetAndRemove,Criteria=" + (criteria == null ? null : criteria.getCriteriaObject()));
		}
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		}
		return this.getMongoTemplate().findAndRemove(query, this.getEntityClass(), this.getCollectionName());
	}

	protected List<T> _listAndRemove(Criteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("ListAndRemove,Criteria=" + (criteria == null ? null : criteria.getCriteriaObject()));
		}
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		}
		return this.getMongoTemplate().findAllAndRemove(query, this.getEntityClass(), this.getCollectionName());
	}

	protected T _getAndUpdate(Criteria criteria, Update update) {
		if (logger.isDebugEnabled()) {
			logger.debug("GetAndUpdate,Criteria=" + (criteria == null ? null : criteria.getCriteriaObject())
					+ "\nUpdate=" + (update == null ? null : update.toString()));
		}
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		}
		return this.getMongoTemplate().findAndModify(query, update, this.getEntityClass(), this.getCollectionName());
	}

	protected boolean _update(Criteria criteria, T dto) {
		DBObject obj = (DBObject) this.getMongoTemplate().getConverter().convertToMongoType(dto);
		return _updateFirst(criteria, Update.fromDBObject(obj));
	}

	protected T _getByProperty(String whereProperty, Object whereValue) {
		return _get(Criteria.where(whereProperty).is(whereValue));
	}

	protected T _getById(Object id) {
		String idPropertyName = this.getIdPropertyName();
		return _get(Criteria.where(idPropertyName).is(id));
	}

	protected T _get(Criteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Get,Criteria=" + (criteria == null ? null : criteria.getCriteriaObject()));
		}
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		}
		return this.getMongoTemplate().findOne(query, this.getEntityClass(), this.getCollectionName());
	}

	protected List<T> _query(Query query) {
		if (logger.isDebugEnabled()) {
			logger.debug("Query=" + query);
		}
		List<T> result = this.getMongoTemplate().find(query, this.getEntityClass(), getCollectionName());
		return result;
	}

	protected MongoPagination<T> _paginatedQuery(Query query, MongoPagination<T> page) {
		int skip = 0;
		int pageNo = page.getPageNo();
		int limitSize = page.getPageLimitSize();
		if (pageNo >= 1 && limitSize > 0) {
			skip = (pageNo - 1) * limitSize;
		}
		Sort sort = null;
		String[] orderBy = page.getOrderBy();
		if (orderBy != null && orderBy.length > 0) {
			sort = new Sort(page.isDesc() ? Direction.DESC : Direction.ASC, orderBy);
		}
		if (query == null) {
			query = new Query();
		}
		if (sort != null) {
			query.with(sort);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Pagination,Query=" + query);
		}
		long totalSize = page.getTotalSize();
		if (totalSize < 0) {
			totalSize = this.getMongoTemplate().count(query, this.getCollectionName());
			page.setTotalSize(totalSize);
		}
		if (skip > 0)
			query.skip(skip);
		if (limitSize > 0)
			query.limit(limitSize);
		List<T> result = this.getMongoTemplate().find(query, this.getEntityClass(), getCollectionName());
		page.setPojos(result);
		return page;
	}

	protected MongoPagination<T> _paginated(Criteria criteria, MongoPagination<T> page) {
		if (criteria == null)
			criteria = new Criteria();
		return _paginatedQuery(Query.query(criteria), page);
	}

	protected MongoPagination<T> _paginated(MongoPagination<T> page) {
		return _paginatedQuery(Query.query(new Criteria()), page);
	}

	protected long _count(Criteria criteria) {
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		} else {
			query = new Query();
		}
		return _countQuery(query);
	}

	protected long _countQuery(Query query) {
		if (logger.isDebugEnabled()) {
			logger.debug("Size,Query=" + query.toString());
		}
		return this.getMongoTemplate().count(query, this.getEntityClass());
	}

	protected boolean _removeById(Object id) {
		return _remove(Criteria.where(this.getIdPropertyName()).is(id)) == 1;
	}

	protected int _removeByProperty(String whereProperty, Object whereValue) {
		return _remove(Criteria.where(whereProperty).is(whereValue));
	}

	protected int _remove(Criteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Remove,Criteria=" + (criteria == null ? null : criteria.getCriteriaObject()));
		}
		Query query = null;
		if (criteria != null) {
			query = Query.query(criteria);
		} else {
			query = new Query();
		}
		WriteResult result = this.getMongoTemplate().remove(query, this.getEntityClass(), getCollectionName());
		if (logger.isDebugEnabled()) {
			logger.debug("Remove,result=" + result);
		}
		return result.getN();
	}

	@Override
	public final void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this._mongoTemplate = initMongoTemplate();
		init();
	}

	protected void init() throws Exception {

	}

	protected static final String DEF_MONGO_TEMPLATE_BEAN_NAME = "def_mongodb_template";

	private final Object lock = new Object();

	protected final MongoTemplate initMongoTemplate() {
		MongoTemplate _mongoTemplate = null;
		ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.getApplicationContext();
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		if (beanFactory.containsBean(DEF_MONGO_TEMPLATE_BEAN_NAME)) {
			_mongoTemplate = beanFactory.getBean(DEF_MONGO_TEMPLATE_BEAN_NAME, MongoTemplate.class);
		}
		if (_mongoTemplate == null) {
			Map<String, MongoTemplate> matchingBeans = beanFactory.getBeansOfType(MongoTemplate.class);
			if (matchingBeans != null && matchingBeans.size() == 1) {
				_mongoTemplate = (MongoTemplate) matchingBeans.values().toArray()[0];
			}
		}
		ConfigurableListableBeanFactory parentBeanFactory = (ConfigurableListableBeanFactory) beanFactory
				.getParentBeanFactory();
		if (_mongoTemplate == null && parentBeanFactory != null) {
			Map<String, MongoTemplate> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(parentBeanFactory,
					MongoTemplate.class, true, false);
			if (matchingBeans != null && matchingBeans.size() == 1) {
				_mongoTemplate = (MongoTemplate) matchingBeans.values().toArray()[0];
			}
		}
		if (_mongoTemplate == null) {
			throw new FatalBeanException("mongoTemplate can't resolved.");
		}
		if (collectionName == null) {
			Class<T> entityClass = this.getEntityClass();
			if (entityClass == null) {
				throw new java.lang.IllegalArgumentException("entityClass can't be resolved.");
			}
			collectionName = _mongoTemplate.getCollectionName(entityClass);
		}
		if (logger.isInfoEnabled()) {
			logger.info("MongodbCollection,name=" + collectionName + ",dtoClazz=" + this.getEntityClass().getName()
					+ (",idPropertyName=" + this.getIdPropertyName(false)));
		}
		DBCollection collection = _mongoTemplate.getCollection(collectionName);
		if (logger.isInfoEnabled()) {
			logger.info("MongodbCollection inited,name=" + collectionName + ",size=" + collection.count());
		}
		return _mongoTemplate;
	}

	public MongoTemplate getMongoTemplate() {
		if (this._mongoTemplate == null) {
			synchronized (lock) {
				if (this._mongoTemplate == null) {
					this._mongoTemplate = initMongoTemplate();
				}
			}
		}
		return _mongoTemplate;
	}

	protected GroupOperation groupOperation(String... fields) {
		return Aggregation.group(fields);
	}

	protected <R> List<R> _groupBy(GroupOperation groupOperation, Criteria criteria, Class<R> outputType) {
		List<AggregationOperation> operations = new ArrayList<AggregationOperation>(7);
		operations.add(groupOperation);
		if (criteria != null) {
			operations.add(Aggregation.match(criteria));
		}
		TypedAggregation<T> aggregationType = Aggregation.newAggregation(this.getEntityClass(), operations);
		AggregationResults<R> sumResult = this.getMongoTemplate().aggregate(aggregationType, this.getCollectionName(),
				outputType);
		return sumResult.getMappedResults();
	}

	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this._mongoTemplate = mongoTemplate;
	}

	protected Criteria where(String key) {
		return new Criteria(key);
	}

	public String getCollectionName() {
		this.getMongoTemplate();// 初始化模板
		if (collectionName == null) {
			throw new NullPointerException("collectionName is null.");
		}
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

}
