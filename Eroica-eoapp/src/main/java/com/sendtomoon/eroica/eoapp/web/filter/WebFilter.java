package com.sendtomoon.eroica.eoapp.web.filter;

import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public interface WebFilter  extends Filter,DisposableBean,InitializingBean{

	List<String> getPatterns();
	
}
