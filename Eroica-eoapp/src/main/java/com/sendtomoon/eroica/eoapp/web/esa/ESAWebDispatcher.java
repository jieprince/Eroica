package com.sendtomoon.eroica.eoapp.web.esa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.HttpRequestHandler;

import com.alibaba.fastjson.JSONObject;
import com.sendtomoon.eroica.common.Eroica;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationController;
import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerException;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.app.dto.SessionDTO;
import com.sendtomoon.eroica.common.beans.json.JsonMapUtils;
import com.sendtomoon.eroica.common.utils.ESAPatternUtils;
import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomoon.eroica.common.utils.ParametersToMapUtils;
import com.sendtomoon.eroica.common.web.WebException;
import com.sendtomoon.eroica.common.web.util.ContentTypeUtil;
import com.sendtomoon.eroica.eoapp.EoApp;

@SuppressWarnings({ "unchecked" })
public class ESAWebDispatcher implements ApplicationContextAware, InitializingBean {

	protected Log logger = LogFactory.getLog(this.getClass());

	private MediaType _jsonMediaType = MediaType.APPLICATION_JSON;

	private MediaType _javaMediaType = new MediaType("application", "x-java-serialized-object");

	private HttpRequestHandler javaRequestHandler;

	private ContentTypeUtil contentTypeUtil = new ContentTypeUtil();

	private ApplicationController dispatcher;

	protected ApplicationContext applicationContext;

	private ParametersToMapUtils parametersToMapUtils = new ParametersToMapUtils();

	@Override
	public void afterPropertiesSet() {
		if (dispatcher == null) {
			dispatcher = EoApp.getInstance();
		}
		if (javaRequestHandler == null) {
			HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
			exporter.setServiceInterface(ApplicationController.class);
			exporter.setService(new ApplicationController() {

				@Override
				public ServiceResponse handleRequest(ServiceRequest request)
						throws ApplicationControllerException, RemoteException {
					return handleESARequest(request);
				}

			});
			exporter.afterPropertiesSet();
			this.javaRequestHandler = exporter;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ServiceRequest newRequestByJson(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ApplicationControllerException {
		String servicesId = getServiceId(request);
		Map<?, ?> params = null;
		StringWriter requestBody = readRequestBody(request);
		String requestParamsString = (requestBody == null ? null : requestBody.toString());
		if (logger.isDebugEnabled()) {
			logger.debug("RequestParamaters(JSON)=" + requestParamsString);
		}
		if (requestParamsString != null && requestParamsString.length() > 10) {
			try {
				params = JsonMapUtils.toHashMap(requestParamsString);
			} catch (Exception ex) {
				throw new WebException(
						"RequestParamaters(JSON)[" + requestParamsString + "] format error:" + ex.getMessage(), ex);
			}
		} else {
			params = new HashMap<Object, Object>(16);
		}
		return newRequest(servicesId, params);
	}

	public ServiceRequest newRequestByForm(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ApplicationControllerException {
		String servicesId = getServiceId(request);
		Map<?, ?> params = formToMap(request);
		if (logger.isDebugEnabled()) {
			logger.debug("RequestParamaters(Form)=" + params);
		}
		return newRequest(servicesId, params);

	}

	protected ServiceRequest newRequest(String actionName, Map<?, ?> params) {
		ServiceRequest sr = new ServiceRequest(actionName, params);
		sr.setSessionDTO(MDCUtil.peekSessionDTO(getRequestId(params)));
		sr.setParameter(Eroica.PARAM_NAME_DATA_TYPE, "map");
		return sr;
	}

	protected SessionDTO getRequestId(Map<?, ?> params) {
		SessionDTO requestId = null;
		Object obj = params.get(ServiceRequest.SESSION_DTO);
		if (obj != null) {
			if (obj instanceof Map) {
				requestId = new SessionDTO();
				Map<?, ?> map = (Map<?, ?>) obj;
				Object v = map.get("txnId");
				if (v != null) {
					requestId.setTxnId(v.toString());
				}
				v = map.get("userID");
				if (v != null) {
					requestId.setUserId(v.toString());
				}
			} else {
				requestId = (SessionDTO) obj;
			}
		}
		return null;
	}

	protected Map<?, ?> formToMap(HttpServletRequest request) {
		Map<?, ?> parameters = request.getParameterMap();
		return parametersToMapUtils.toMap(parameters);
	}

	protected String getServiceId(HttpServletRequest request) {
		String uri = request.getRequestURI();
		uri = uri.replace('\\', '/');
		String esaName = uri.substring(uri.lastIndexOf('/') + 1);
		ESAPatternUtils.check(esaName);
		return esaName;
	}

	public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
		ServiceRequest sreq = null;
		if (isJsonRequest(httpRequest)) {
			sreq = this.newRequestByJson(httpRequest, httpResponse);
		} else if (isJavaRequest(httpRequest)) {
			javaRequestHandler.handleRequest(httpRequest, httpResponse);
		} else {
			sreq = this.newRequestByForm(httpRequest, httpResponse);
		}
		if (sreq != null) {
			initServiceRequest(sreq, httpRequest, httpResponse);
			ServiceResponse resp = handleESARequest(sreq);
			String charset = getCharset(httpResponse);
			byte[] jsonDatas = null;
			if (resp != null) {
				processResponse(resp);
				jsonDatas = getResponseJsonBytes(resp, charset);
			}
			handleJsonDatas(jsonDatas, httpRequest, httpResponse);
			outputHttpResponse(jsonDatas, httpResponse);
		}
	}

	protected byte[] handleJsonDatas(byte[] jsonDatas, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		return jsonDatas;
	}

	protected void initServiceRequest(ServiceRequest serviceRequest, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {

	}

	protected ServiceResponse handleESARequest(ServiceRequest sreq)
			throws ApplicationControllerException, RemoteException {
		return dispatcher.handleRequest(sreq);
	}

	protected boolean isJsonRequest(HttpServletRequest httpRequest) {
		return httpRequest.getMethod().equals("POST")
				&& _jsonMediaType.includes(contentTypeUtil.getMediaType(httpRequest));
	}

	protected boolean isJavaRequest(HttpServletRequest httpRequest) {
		return httpRequest.getMethod().equals("POST")
				&& _javaMediaType.includes(contentTypeUtil.getMediaType(httpRequest));
	}

	protected void outputHttpResponse(byte[] jsonDatas, HttpServletResponse response) throws IOException {
//		response.setContentType("text/html;charset="+response.getCharacterEncoding());
		response.setContentType("application/json;charset=" + response.getCharacterEncoding());
		response.setContentLength(jsonDatas == null ? 0 : jsonDatas.length);
		//
		if (jsonDatas != null) {
			OutputStream out = response.getOutputStream();
			out.write(jsonDatas);
			out.flush();
		} else {
			logger.warn("Response model is null.");
		}
	}

	protected String getCharset(HttpServletResponse response) {
		String charset = response.getCharacterEncoding();
		if (charset == null) {
			charset = "UTF-8";
			response.setCharacterEncoding(charset);
		}
		return charset;
	}

	protected byte[] getResponseJsonBytes(ServiceResponse servicesResponse, String charset) throws IOException {

		Map<?, ?> result = servicesResponse.getModel();
		if (result == null) {
			return null;
		}
		String respString = JSONObject.toJSONString(result);
		if (logger.isDebugEnabled()) {
			logger.debug("ResponseJSON=" + respString);
		}
		return respString.getBytes(charset);
	}

	private void processResponse(ServiceResponse resp) {
		Map<Object, Object> model = resp.getModel();
		if (model == null) {
			model = new HashMap<Object, Object>(2);
			model.put("responseCode", resp.getResponseCode());
			if (resp.getResponseMsg() != null) {
				model.put("responseMsg", resp.getResponseMsg());
			}
			resp.setModel(model);
		} else {
			if (!model.containsKey("responseCode")) {
				model.put("responseCode", resp.getResponseCode());
			}
			if (resp.getResponseMsg() != null && !model.containsKey("responseMsg")) {
				model.put("responseMsg", resp.getResponseMsg());
			}
		}
	}

	protected StringWriter readRequestBody(HttpServletRequest request) {
		try {
			BufferedReader in = request.getReader();
			StringWriter out = null;
			// ------------------------------------
			char[] buffer = new char[512];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				if (out == null) {
					out = new StringWriter(512);
				}
				out.write(buffer, 0, bytesRead);
			}
			return out;
		} catch (IOException e) {
			throw new WebException("Read request body error:" + e.getMessage(), e);
		}

	}

	public ApplicationController getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(ApplicationController dispatcher) {
		this.dispatcher = dispatcher;
	}

}
