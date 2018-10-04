package com.sendtomoon.eroica.eoapp.context.support;

import java.rmi.RemoteException;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationController;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerException;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;



public class EoAppLocalApplicationController implements ApplicationController{


	@Override
	public ServiceResponse handleRequest(ServiceRequest request)
			throws ApplicationControllerException, RemoteException {
		return com.sendtomoon.eroica.eoapp.EoApp.getInstance().handleRequest(request);
	}

}
