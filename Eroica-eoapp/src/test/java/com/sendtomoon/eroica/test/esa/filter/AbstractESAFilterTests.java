package com.sendtomoon.eroica.test.esa.filter;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.ui.ModelMap;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationController;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerException;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.esa.filter.AbstractESAFilter;
import com.sendtomoon.eroica.eoapp.esa.filter.DefESAFilterChain;
import com.sendtomoon.eroica.eoapp.esa.filter.ESAFilterChain;

public class AbstractESAFilterTests {

	public static void main(String args[]) throws Throwable {
		SampleESAFilter filter = new SampleESAFilter();
		Set<String> set = new HashSet<String>();
		set.add("test.abc");
		set.add("test.abc1");
		set.add("test.abc3");
		// filter.setFiltActionName("test.abc31");
		// filter.setExcludeSet(set);
		filter.setIncludeSet(set);
		DefESAFilterChain chain = new DefESAFilterChain(new ApplicationController() {

			@Override
			public ServiceResponse handleRequest(ServiceRequest request)
					throws ApplicationControllerException, RemoteException {

				ServiceResponse sr = new ServiceResponse(new ModelMap());
				System.err.println("22222====>执行请求。。。。");
				return sr;
			}

		}, filter);
		ServiceRequest sr = new ServiceRequest("test.abc31", new ModelMap());
		chain.doFilter(sr);
	}
}

class SampleESAFilter extends AbstractESAFilter {

	@Override
	public ServiceResponse doFilter(ServiceRequest request, ESAFilterChain chain) throws Throwable {
		System.err.println("11111====>进入Filter.....");
		return chain.doFilter(request);
	}

}
