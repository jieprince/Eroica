package com.sendtomoon.eroica.eoapp.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sendtomoon.eroica.eoapp.sar.SARContext;

public interface WebSAResolver {

	List<SARContext>  resolve(List<SARContext> sars
			,HttpServletRequest request,HttpServletResponse response);
	
}
