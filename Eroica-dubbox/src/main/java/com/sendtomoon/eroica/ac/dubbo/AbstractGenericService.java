package com.sendtomoon.eroica.ac.dubbo;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.SessionDTO;
import com.sendtomoon.eroica.common.utils.MDCData;
import com.sendtomoon.eroica.common.utils.MDCUtil;



public abstract class AbstractGenericService {
	
	protected  Log logger=LogFactory.getLog(this.getClass());
	
	

	protected void forSessionDTO(ServiceRequest actionRequest) {
		SessionDTO s=actionRequest.getSessionDTO();
		if(s!=null && s.getTxnId()!=null){
			MDCUtil.set(s.getTxnId(),s.getUserId());
		}else{
			MDCData data=MDCUtil.peek();
			if(data.getRequestId()!=null){
				data.set(MDCData.KEY_REQUEST_ID,MDCUtil.generateRequestId());
			}
		}
	}
	
	protected Map getParams(Object[] args){
		Object param0=(args!=null && args.length>0)?args[0]:null;
		Map params=(param0==null?null:((GenericParam)param0).getParams());
		return params;
	}
	
}
