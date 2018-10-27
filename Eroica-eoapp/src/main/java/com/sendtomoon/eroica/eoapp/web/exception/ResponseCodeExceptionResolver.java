package com.sendtomoon.eroica.eoapp.web.exception;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
//import org.unidal.cat.Cat;

import com.alibaba.fastjson.JSON;
import com.sendtomoon.eroica.common.exception.ExceptionLogUtils;
import com.sendtomoon.eroica.common.exception.ResponseCodeException;
import com.sendtomoon.eroica.common.utils.MDCUtil;

public class ResponseCodeExceptionResolver extends AbstractHandlerExceptionResolver {

	private ExceptionLogUtils exceptionLogUtils = new ExceptionLogUtils();

	private static ModelAndView empty = new ModelAndView("emptyView");

	private MessageSource messageSource;

	private Locale locale = Locale.CHINA;

	private boolean enableLog = true;

	public ExceptionLogUtils getExceptionLogUtils() {
		return exceptionLogUtils;
	}

	public void setExceptionLogUtils(ExceptionLogUtils exceptionLogUtils) {
		this.exceptionLogUtils = exceptionLogUtils;
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		ResponseCodeException codeEx = ResponseCodeException.toResponseCodeException(ex);
		if (codeEx != null) {
			String msg = resolveMessage(codeEx);
			codeEx.setResponseMsg(msg);
			if (enableLog) {
				exceptionLogUtils.logResponseCodeException(codeEx, request, this.logger);
			}
			ModelMap model = new ModelMap();
			model.put("responseCode", codeEx.getResponseCode());
			model.put("responseMsg", msg);
			model.put("requestId", MDCUtil.peek().getRequestId());
			String charset = getCharset(response);

//			String code = codeEx.getResponseCode() != null ? codeEx.getResponseCode() : msg != null ? msg : "";
//			Cat.logEvent("Resp.Code", code,code,msg);
//			Cat.logError(""+(code!=null?"ErrorResponse：["+code+"]":"")+(msg!=null?msg:""),ex);		

			String respString = JSON.toJSONString(model);
			if (logger.isDebugEnabled()) {
				logger.debug("ResponseJSON=" + respString);
			}
			try {
				byte[] jsonDatas = respString.getBytes(charset);
				outputHttpResponse(jsonDatas, response);
			} catch (IOException ioEx) {
				logger.error("Output response error,cause:" + ioEx.getMessage());
			}
		}
		return empty;
	}

	protected void outputHttpResponse(byte[] jsonDatas, HttpServletResponse response) throws IOException {
		if (response.getContentType() == null) {
			response.setContentType("application/json");
		}
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

	@Override
	protected void logException(Exception ex, HttpServletRequest request) {

	}

	protected String resolveMessage(ResponseCodeException ex) {
		return ex.resolveMessage(this.getMessageSource(), this.getLocale());
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public boolean isEnableLog() {
		return enableLog;
	}

	public void setEnableLog(boolean enableLog) {
		this.enableLog = enableLog;
	}

}
