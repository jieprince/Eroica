package com.sendtomoon.eroica.eoapp.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebRequestHandler {
	
	
	 void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception  ;
	 
	  
}
