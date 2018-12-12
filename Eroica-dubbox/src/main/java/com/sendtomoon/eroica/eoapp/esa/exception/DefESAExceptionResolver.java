package com.sendtomoon.eroica.eoapp.esa.exception;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
//import org.unidal.cat.Cat;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.common.exception.ResponseCodeException;
import com.sendtomoon.eroica.common.utils.MDCUtil;

public class DefESAExceptionResolver implements ESAExceptionResolver, Ordered {

	private boolean logError = true;

	private MessageSource messageSource;

	private Locale locale = Locale.CHINA;

	private Log logger = LogFactory.getLog(DefESAExceptionResolver.class);

	private int order = Ordered.LOWEST_PRECEDENCE;

	@Override
	public ServiceResponse resolveException(ServiceRequest request, Throwable ex) {
		String esaName = request.getRequestedServiceID();
		ResponseCodeException codeEx = findResponseCodeException(ex);
		String code = null;
		if (isLogError()) {
			String msg = codeEx.getMessage();
			code = codeEx.getResponseCode();
			logger.error("Handle esa<" + esaName + "> error" + (code != null ? ",responseCode=" + code : "")
					+ (msg != null ? ",cause:" + msg : ""), ex);
		}

		ServiceResponse resp = resolveExceptionServiceResponse(codeEx);
		return resp;
	}

	protected ResponseCodeException findResponseCodeException(Throwable ex) {
		return ResponseCodeException.toResponseCodeException(ex);
	}

	protected ServiceResponse resolveExceptionServiceResponse(ResponseCodeException ex) {
		ServiceResponse resp = new ServiceResponse();
		Map<Object, Object> model = new HashMap<Object, Object>(8);
		String msg = resolveMessage(ex);
		resp.setResponseCode(ex.getResponseCode());
		resp.setResponseMsg(msg);
		model.put("responseCode", ex.getResponseCode());
		model.put("responseMsg", msg);
		model.put("requestId", MDCUtil.peek().getRequestId());

		resp.setModel(model);
		return resp;
	}

	protected String resolveMessage(ResponseCodeException ex) {
		return ex.resolveMessage(this.getMessageSource(), this.getLocale());
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public boolean isLogError() {
		return logError;
	}

	public void setLogError(boolean logError) {
		this.logError = logError;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
