package com.sendtomoon.eroica.eoapp.esa.annotation;

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.core.MethodParameter;
import org.springframework.util.ClassUtils;

import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.beans.map.BeanMapUtils;

public class MethodESAFactory implements FactoryBean<MethodESA> {

	private Method method;

	private Method interfaceMethod;

	private String beanName;

	private String methodName;

	private Class<?> targetClazz;

	protected MethodESA esa;

	private String esaName;

	private BeanMapUtils beanMapUtils;

	public MethodESAFactory() {
	}

	public MethodESAFactory(String esaName, String beanName, Method method, Class<?> targetClazz,
			BeanMapUtils beanMapUtils) {
		this.esaName = esaName;
		this.method = method;
		this.beanName = beanName;
		this.targetClazz = targetClazz;
		this.beanMapUtils = beanMapUtils;
	}

	public MethodESAFactory(String esaName, String beanName, Method method, Class<?> targetClazz) {
		this.esaName = esaName;
		this.method = method;
		this.beanName = beanName;
		this.targetClazz = targetClazz;
		this.beanMapUtils = new BeanMapUtils();
	}

	@Override
	public MethodESA getObject() throws BeansException {
		if (esa != null) {
			return esa;
		}
		try {
			if ((esaName = esaName.trim()).length() == 0) {
				throw new NullPointerException("esaName required.");
			}
			if ((beanName = beanName.trim()).length() == 0) {
				throw new NullPointerException("beanName required.");
			}
			if (method == null && methodName == null) {
				throw new NullPointerException("methodName required.");
			}
			if (targetClazz == null) {
				throw new NullPointerException("targetClazz required.");
			}
			//
			esa = new MethodESA(beanMapUtils == null ? new BeanMapUtils() : beanMapUtils);
			esa.setName(esaName);
			initMethod();
			// ------------------------
			initParamaters();
			// ------------------------
			initResult();
			// -----------------------------
			esa.setBeanName(beanName);
			esa.setBeanMethod(interfaceMethod != null ? interfaceMethod : this.method);
		} catch (Throwable th) {
			if (th instanceof BeanDefinitionValidationException) {
				throw new BeanDefinitionValidationException("Bean<" + beanName + "> export ESA<" + esaName + ">"
						+ " defined error,casue:" + th.getMessage());
			} else {
				throw new BeanDefinitionValidationException("Bean<" + beanName + "> export ESA<" + esaName + ">"
						+ " defined error,casue:" + th.getMessage(), th);
			}

		}
		return esa;
	}

	@Override
	public Class<?> getObjectType() {
		return MethodESA.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	protected void initMethod() {
		if (methodName != null) {
			Method[] declaredMethods = targetClazz.getDeclaredMethods();
			int count = 0;
			for (Method method : declaredMethods) {
				if (methodName.equals(method.getName())) {
					this.method = method;
					count++;
				}
			}
			if (count > 1) {
				throw new BeanDefinitionValidationException(
						"Method:" + methodName + " Overloading, can't export for esa.");
			}
			if (count == 0) {
				throw new BeanDefinitionValidationException("Not found method:" + methodName);
			}
		}
		interfaceMethod = getInterfaceMethod(this.method, targetClazz);
		//
		if ((method.getModifiers() & 1) == 0) {
			throw new BeanDefinitionValidationException(" method<" + method.getName() + "> not be public.");
		}
	}

	protected void initResult() {
		Class<?> returnType = this.method.getReturnType();
		if (returnType == null || returnType.equals(Void.class)) {
			return;
		}
		MethodResultInfo resultInfo = new MethodResultInfo(returnType);
		if (ServiceResponse.class.isAssignableFrom(returnType)) {
			resultInfo.setServiceResponse(true);
		}
		esa.setResultInfo(resultInfo);
	}

	protected void initParamaters() {
		Class<?> types[] = this.method.getParameterTypes();
		if (types == null || types.length == 0)
			return;
		// -------------------
		MethodParameterInfo[] paramInfos = new MethodParameterInfo[types.length];
		for (int i = 0; i < types.length; i++) {
			paramInfos[i] = new MethodParameterInfo(new MethodParameter(method, i));
		}
		esa.setParamInfos(paramInfos);
	}

	private Method getInterfaceMethod(Method m, Class<?> targetClazz) {
		Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(targetClazz);
		if (interfaces == null)
			return null;
		for (Class<?> intf : interfaces) {
			Method temp = ClassUtils.getMethodIfAvailable(intf, m.getName(), m.getParameterTypes());
			if (temp != null)
				return temp;
		}
		return null;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getEsaName() {
		return esaName;
	}

	public void setEsaName(String esaName) {
		this.esaName = esaName;
	}

	public BeanMapUtils getBeanMapUtils() {
		return beanMapUtils;
	}

	public void setBeanMapUtils(BeanMapUtils beanMapUtils) {
		this.beanMapUtils = beanMapUtils;
	}

}
