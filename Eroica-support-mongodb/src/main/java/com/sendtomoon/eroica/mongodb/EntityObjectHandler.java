package com.sendtomoon.eroica.mongodb;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.data.annotation.Id;

public class EntityObjectHandler<T> extends ApplicationObjectSupport implements InitializingBean {

	protected Log logger = LogFactory.getLog(this.getClass());

	private Class<T> entityClass;

	private String idPropertyName;

	private PropertyDescriptor idPropertyDescriptor;

	protected Class<T> getEntityClass() {
		return entityClass;
	}

	@SuppressWarnings("unchecked")
	private Class<T> resolveEntityClassByGeneric() {
		Type type = this.getClass().getGenericSuperclass();
		if (type != null && type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			Type[] types = ptype.getActualTypeArguments();
			if (types != null && types.length > 0) {
				Type genericClass = types[0];
				if (genericClass != null && genericClass instanceof Class) {
					Class<T> genericClazz = (Class<T>) genericClass;
					String genericClazzName = genericClazz.getName();
					if (!(genericClazz.isInterface() || genericClazzName.startsWith("java."))) {
						return genericClazz;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (entityClass == null) {
			entityClass = resolveEntityClassByGeneric();
		}
		if (entityClass != null) {
			PropertyDescriptor[] propertyDescriptors = null;
			try {
				propertyDescriptors = Introspector.getBeanInfo(entityClass).getPropertyDescriptors();
			} catch (IntrospectionException e) {
				throw new FatalBeanException(e.getMessage(), e);
			}
			this.idPropertyDescriptor = resolveIdProperty(propertyDescriptors, entityClass);
			if (idPropertyDescriptor != null) {
				idPropertyName = idPropertyDescriptor.getName();
			}
		}

	}

	private PropertyDescriptor resolveIdProperty(PropertyDescriptor[] properties, Class<?> entityClass) {
		if (properties == null) {
			return null;
		}
		return findPropertyDescriptor(properties, entityClass, Id.class.getSimpleName());
	}

	private PropertyDescriptor findPropertyDescriptor(PropertyDescriptor[] properties, Class<?> entityClass,
			String annotationName) {
		if (properties == null) {
			return null;
		}
		PropertyDescriptor find = null;
		for (PropertyDescriptor property : properties) {
			try {
				Field field = entityClass.getDeclaredField(property.getName());
				if (field != null && isIncludedAnnotation(field.getDeclaredAnnotations(), annotationName)) {
					find = property;
					break;
				}
			} catch (Exception e) {
			}
			Method readMethod = property.getReadMethod();
			if (readMethod != null && isIncludedAnnotation(readMethod.getDeclaredAnnotations(), annotationName)) {
				find = property;
				break;
			}
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null && isIncludedAnnotation(writeMethod.getDeclaredAnnotations(), annotationName)) {
				find = property;
				break;
			}
		}
		if (find != null) {
			if (find.getWriteMethod() == null) {
				throw new FatalBeanException("Id property<" + find.getName() + "> no wirte method.");
			}
			if (find.getReadMethod() == null) {
				throw new FatalBeanException("Id property<" + find.getName() + "> no read method.");
			}
			return find;
		}
		return null;

	}

	private boolean isIncludedAnnotation(Annotation annotations[], String annotationName) {
		if (annotations == null || annotations.length == 0) {
			return false;
		}
		boolean flag = false;
		for (Annotation annotation : annotations) {
			String target = annotation.annotationType().getSimpleName();
			if (target.equals(annotationName)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	protected String getIdPropertyName(boolean required) {
		if (idPropertyName == null && required) {
			throw new java.lang.IllegalArgumentException("not spec id property by annotation:" + Id.class.getName());
		}
		return idPropertyName;
	}

	protected String getIdPropertyName() {
		return getIdPropertyName(true);
	}

	protected Object getIdValue(T entity) {
		if (this.idPropertyDescriptor == null) {
			throw new java.lang.IllegalArgumentException("IdPropertyDescriptor be null.");
		}
		Method readMethod = idPropertyDescriptor.getReadMethod();
		if (readMethod == null) {
			throw new java.lang.IllegalArgumentException("IdProperty<" + this.idPropertyName + "> no read method.");
		}
		Object idValue = null;
		try {
			idValue = readMethod.invoke(entity);
		} catch (Exception e) {
		}
		if (idValue == null) {
			throw new java.lang.IllegalArgumentException(
					"IdProperty<" + this.idPropertyName + "> value be null by entity=" + entity);
		}
		return idValue;
	}

}
