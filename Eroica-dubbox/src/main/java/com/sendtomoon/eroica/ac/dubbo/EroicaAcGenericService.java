package com.sendtomoon.eroica.ac.dubbo;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerLocal;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;

public class EroicaAcGenericService extends AbstractGenericService implements GenericService {

	private ApplicationControllerLocal dispatcher;

	public EroicaAcGenericService(ApplicationControllerLocal dispatcher) {
		this.dispatcher = dispatcher;
	}

	public EroicaAcGenericService() {
	}

	public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
		RpcContext context = RpcContext.getContext();
		String interfaceName = context.getUrl().getParameter(Constants.INTERFACE_KEY);
		if (interfaceName == null || interfaceName.length() == 0) {
			interfaceName = context.getUrl().getPath();
		}
		if (interfaceName == null) {
			throw new java.lang.IllegalArgumentException("Requried <interface>  in RpcContext.");
		}
		ServiceRequest request = getRequest(interfaceName, args);
		forSessionDTO(request);
		//
		ServiceResponse response = dispatcher.handleRequest(request);
		return toGenericResult(response);
	}

	protected ServiceRequest getRequest(String interfaceName, Object[] args) {
		Map params = getParams(args);
		if (logger.isDebugEnabled()) {
			logger.debug("Handle interface<" + interfaceName + ">.");
		}
		return new ServiceRequest(interfaceName, params);
	}

	protected GenericResult toGenericResult(ServiceResponse response) {
		if (response == null) {
			return null;
		}
		Map<?, ?> model = response.getModel();
		if (model == null) {
			Map<Object, Object> temp = new HashMap<Object, Object>();
			temp.put("responseCode", response.getResponseCode());
			temp.put("responseMsg", response.getResponseMsg());
			model = temp;
		}
		return new GenericResult(model);
	}

	public ApplicationControllerLocal getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(ApplicationControllerLocal dispatcher) {
		this.dispatcher = dispatcher;
	}

}
