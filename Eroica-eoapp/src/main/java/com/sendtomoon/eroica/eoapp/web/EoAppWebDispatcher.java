package com.sendtomoon.eroica.eoapp.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface EoAppWebDispatcher {

	void dispatchRequest(HttpServletRequest processedRequest
			, HttpServletResponse response) throws ServletException;
}
