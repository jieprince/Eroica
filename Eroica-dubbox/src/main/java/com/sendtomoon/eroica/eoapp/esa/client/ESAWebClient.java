package com.sendtomoon.eroica.eoapp.esa.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ESAWebClient {

	
	void handleRequest(String esaName,HttpServletRequest request,HttpServletResponse response);
}
