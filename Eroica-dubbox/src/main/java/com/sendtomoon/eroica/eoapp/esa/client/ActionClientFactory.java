package com.sendtomoon.eroica.eoapp.esa.client;

import com.sendtomoon.eroica.common.appclient.IServiceClient;

public interface ActionClientFactory {

	IServiceClient get(String actionName);
	
	IServiceClient get(String actionName,String group);
}
