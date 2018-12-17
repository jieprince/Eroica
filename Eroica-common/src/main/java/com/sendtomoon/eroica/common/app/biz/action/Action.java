/*
 * Created on 2005-2-4
 *
 *
 *
 */
package com.sendtomoon.eroica.common.app.biz.action;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;

/**
 * Action is where application logic resides. It is closely related with
 * user request.
 * 
 * @author architect
 * 
 */
public interface Action {
	public ServiceResponse perform(ServiceRequest request)
	throws Exception;

}
