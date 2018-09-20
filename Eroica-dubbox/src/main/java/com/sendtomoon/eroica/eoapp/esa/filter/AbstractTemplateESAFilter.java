package com.sendtomoon.eroica.eoapp.esa.filter;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;

/**
 * <p>
 * Title: AbstractTemplateESAFilter
 * </p>
 * <p>
 * Description: 模板过滤器，级别为最高
 * </p>
 * <p>
 * Copyright: PA TRUST Copyright (c) 2015
 * </p>
 * <p>
 * Company: PA TRUST
 * </p>
 *
 * @author 谢轶丹
 *
 * @version
 * @since 2015年1月17日
 */
public abstract class AbstractTemplateESAFilter extends AbstractESAFilter {

	
	/**
	 * 过滤前执行
	 * 
	 * @param request
	 */
	abstract protected void preFilter(ServiceRequest request);

	/**
	 * 过滤后执行
	 * 
	 * @param request
	 * @param ex
	 *            产生的异常，可以为空
	 */
	abstract protected void afterFilter(ServiceRequest request,
			ServiceResponse response, Throwable ex);

	@Override
	public ServiceResponse doFilter(ServiceRequest request, ESAFilterChain chain)
			throws Throwable {
		Throwable tx = null;
		ServiceResponse resp = null;
		try {
			this.preFilter(request);
			resp = chain.doFilter(request);
			return resp;
		} catch (Throwable ex) {
			tx = ex;
			throw ex;
		} finally {
			this.afterFilter(request, resp, tx);
		}
	}

	@Override
	public abstract int getOrder();

}
