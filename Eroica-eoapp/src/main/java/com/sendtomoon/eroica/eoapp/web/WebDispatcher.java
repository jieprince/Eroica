package com.sendtomoon.eroica.eoapp.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface WebDispatcher {
	

	boolean handleRequest(HttpServletRequest request, HttpServletResponse response
			,String[] patterns) throws ServletException  ;
	
	
}
