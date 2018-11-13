package com.sendtomoon.eroica.eoapp.esa.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.beans.map.BeanMapUtils;
import com.sendtomoon.eroica.common.biz.action.ActionException;
import com.sendtomoon.eroica.common.exception.ResponseCodeException;
import com.sendtomoon.eroica.common.validator.ValidateFailException;

public class MethodESA {

	private String name;

	private MethodParameterInfo[] paramInfos;

	private MethodResultInfo resultInfo;

	private Method beanMethod;

	private String beanName;

	private BeanMapUtils beanMapUtils;

	public MethodESA(BeanMapUtils beanMapUtils) {
		this.beanMapUtils = beanMapUtils;
	}

	public String getName() {
		return name;
	}

	public ServiceResponse perform(ServiceRequest request, ConfigurableApplicationContext applicationContext,
			Validator validator) {
		return perform(request, applicationContext, null, validator);
	}

	public ServiceResponse perform(ServiceRequest request, ConfigurableApplicationContext applicationContext,
			List<AnnotationArgumentResolver> argumentResolvers, Validator validator) {
		try {
			Object[] methodArgs = getMethodArgs(request, argumentResolvers);
			if (validator != null)
				validateArgs(methodArgs, validator);
			Object returnValue = invokeMethod(methodArgs, applicationContext);
			//
			return getServiceResponse(returnValue, request);
		} catch (Throwable th) {
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			} else {
				throw new ActionException(th.getMessage(), th);
			}
		}
	}

	protected void validateArgs(Object[] args, Validator validator) throws ValidateFailException {
		if (paramInfos == null || paramInfos.length == 0) {
			return;
		}
		// --------------
		for (int i = 0; i < paramInfos.length; i++) {
			MethodParameterInfo info = paramInfos[i];
			Object value = args[i];
			if (info != null && info.isValidateable() && value != null) {
				// ---------
				Errors errors = new BeanPropertyBindingResult(value, value.getClass().getName());
				validator.validate(value, errors);
				if (((i + 1) != paramInfos.length) && paramInfos[i + 1].isBindResult() && args[i + 1] == null) {
					args[i + 1] = errors;
				} else {
					if (errors.getErrorCount() > 0) {
						throw new ValidateFailException(value.getClass(), errors);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object[] getMethodArgs(ServiceRequest request, List<AnnotationArgumentResolver> argumentResolvers)
			throws Exception {
		if (paramInfos == null || paramInfos.length == 0) {
			return null;
		}
		Map<Object, Object> params = request.getParameters();
		Object[] arr = new Object[paramInfos.length];
		for (int i = 0; i < paramInfos.length; i++) {
			MethodParameterInfo info = paramInfos[i];
			try {
				if (info.isServiceRequest()) {
					arr[i] = request;
				} else if (info.isModelAttribute()) {
					String name = info.getName();
					Object value = params;
					if (name != null) {
						value = params.get(name);
					}
					if (value == null) {
						value = info.getDefValue();
					}
					if (value == null && info.isRequired()) {
						throw new ResponseCodeException(ResponseCodeException.ERROR_VALIDATE_FAIL,
								"parameter:" + info.getName() + " required.");
					}
					if (value != null) {
						arr[i] = beanMapUtils._toBean(info, value);
					}
				} else {
					if (argumentResolvers != null && argumentResolvers.size() > 0) {
						for (AnnotationArgumentResolver argumentResolver : argumentResolvers) {
							Annotation annotation = info.findAnnotation(argumentResolver.getAnnotationClass());
							if (annotation != null) {
								arr[i] = argumentResolver.resolveArgument(info.getMeta(), annotation, request);
								break;
							}
						}
					}
				}
			} catch (ActionException ex) {
				throw ex;
			} catch (Throwable e) {
				throw new ActionException("ESA<" + this.name + "> parameter<" + info.getName()
						+ "> transform error,cause:" + e.getMessage(), e);
			}
		}
		//
		return arr;
	}

	protected Object invokeMethod(Object[] methodArgs, ConfigurableApplicationContext applicationContext)
			throws Throwable {
		Object target = applicationContext.getBean(beanName);
		try {
			return this.beanMethod.invoke(target, methodArgs);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	protected ServiceResponse getServiceResponse(Object returnValue, ServiceRequest request) throws Throwable {
		if (resultInfo.isServiceResponse()) {
			return (ServiceResponse) returnValue;
		} else {
			Map<?, ?> responseModel = null;
			if (returnValue != null) {
				responseModel = getResponseModel(returnValue, request);
			}
			return new ServiceResponse(responseModel);
		}
	}

	protected Map<?, ?> getResponseModel(Object returnValue, ServiceRequest request) throws Throwable {
		// boolean
		// isReturnMap=Pafa.DATA_TYPE_MAP.equals(request.getParameter(Pafa.PARAM_NAME_DATA_TYPE));
		try {
			Object temp = beanMapUtils._toMap(returnValue);
			//
			if (temp instanceof Map) {
				return (Map<?, ?>) temp;
			} else if (temp.getClass().isArray() || temp instanceof Collection) {
				Map<Object, Object> model = new HashMap<Object, Object>();
				model.put("responseCode", "0");
				model.put("datas", temp);
				return model;
			} else {
				Map<Object, Object> model = new HashMap<Object, Object>();
				model.put("responseCode", temp.toString());
				return model;
			}
		} catch (Throwable e) {
			throw new ActionException("ESA[" + this.name + "] result transform to map error.", e);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public MethodParameterInfo[] getParamInfos() {
		return paramInfos;
	}

	public void setParamInfos(MethodParameterInfo[] paramInfos) {
		this.paramInfos = paramInfos;
	}

	public MethodResultInfo getResultInfo() {
		return resultInfo;
	}

	public void setResultInfo(MethodResultInfo resultInfo) {
		this.resultInfo = resultInfo;
	}

	public Method getBeanMethod() {
		return beanMethod;
	}

	public void setBeanMethod(Method beanMethod) {
		this.beanMethod = beanMethod;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

}
