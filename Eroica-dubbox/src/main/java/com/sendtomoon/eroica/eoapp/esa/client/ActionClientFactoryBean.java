package com.sendtomoon.eroica.eoapp.esa.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sendtomoon.eroica.common.Eroica;
import com.sendtomoon.eroica.common.appclient.DefaultActionClient;
import com.sendtomoon.eroica.common.appclient.IServiceClient;

public class ActionClientFactoryBean implements ActionClientFactory, ApplicationContextAware {

	private volatile Map<String, IServiceClient> cache = new ConcurrentHashMap<String, IServiceClient>();

	private ApplicationContext context;

	private String eroicaAc;

	public String getEroicaAc() {
		return eroicaAc;
	}

	public void setEroicaAc(String eroicaAc) {
		this.eroicaAc = eroicaAc;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public IServiceClient get(String actionName) {
		return get(actionName, null);
	}

	@Override
	public IServiceClient get(String actionName, String group) {
		if (actionName == null || (actionName = actionName.trim()).length() == 0) {
			throw new NullPointerException("actionName is null.");
		}
		String id = (group == null ? "" : group + "/") + actionName;
		IServiceClient serviceClient = cache.get(id);
		if (serviceClient == null) {
			synchronized (cache) {
				serviceClient = createServiceClient(actionName, group);
				cache.put(id, serviceClient);
			}
		}
		return serviceClient;
	}

	protected IServiceClient createServiceClient(String actionName, String group) {
		DefaultActionClient temp = new DefaultActionClient();
		temp.setApplicationContext(context);
		temp.setDataType(Eroica.DATA_TYPE_MAP);
		temp.setEroicaAc(eroicaAc);
		temp.setGroup(group);
		temp.setName(actionName);
		temp.setHandleClazz(this.getClass());
		try {
			temp.afterPropertiesSet();
		} catch (Exception e) {
			throw new FatalBeanException("action:" + actionName + " define error,cause:" + e.getMessage(), e);
		}
		return temp;
	}

}
