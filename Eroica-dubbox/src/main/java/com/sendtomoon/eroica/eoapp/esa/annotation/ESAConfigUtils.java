package com.sendtomoon.eroica.eoapp.esa.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.sendtomoon.eroica.eoapp.ESA;

public class ESAConfigUtils {

	private static Log logger = LogFactory.getLog(ESAConfigUtils.class);

	public static Properties getESAConfig(ESA annotation) {
		Properties config = null;
		Class<?> annotationClass = ESA.class;
		Method[] methods = annotationClass.getMethods();
		for (Method method : methods) {
			if (method.getDeclaringClass() != Object.class && method.getReturnType() != void.class
					&& method.getParameterTypes().length == 0 && Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers())) {
				try {
					String property = method.getName();
					if ("name".equals(property) || "value".equals(property) || "toString".equals(property)) {
						continue;
					}
					Object value = method.invoke(annotation, new Object[0]);
					if (value != null && !value.equals(method.getDefaultValue())) {
						if (config == null) {
							config = new Properties();
						}
						if (property.equals("local")) {
							property = "scope";
							value = "local";
						}
						if (value.getClass().isArray()) {
							Object[] arr = (Object[]) value;
							if (arr.length > 0)
								config.setProperty(property, StringUtils.arrayToCommaDelimitedString(arr));
						} else {
							config.setProperty(property, value.toString());
						}

					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return config;
	}
}
