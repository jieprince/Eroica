package com.sendtomoon.eroica.dubbo.ws.consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WeServiceConsumer {

	
	void handleRequest(String serviceId,HttpServletRequest request,HttpServletResponse response);
}
