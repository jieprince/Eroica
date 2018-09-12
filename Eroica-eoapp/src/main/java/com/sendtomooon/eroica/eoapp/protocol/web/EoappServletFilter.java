package com.sendtomooon.eroica.eoapp.protocol.web;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pingan.pafa.pizza.Pizza;
import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;
import com.sendtomooon.eroica.eoapp.EoApp;
import com.sendtomooon.eroica.eoapp.context.config.EoAppConstants;

public class EoappServletFilter implements Filter, java.io.Serializable {

	private static final long serialVersionUID = -4648046053769354724L;

	private EoApp eoapp;

	private String[] resourcesSuffix;

	@Override
	public void destroy() {
		eoapp.shutdown();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest processedRequest = (HttpServletRequest) request;
		HttpServletResponse response = (HttpServletResponse) resp;
		boolean isResource = false;
		if (resourcesSuffix != null && resourcesSuffix.length > 0) {
			String uri = processedRequest.getRequestURI().toLowerCase();
			for (int i = 0; i < resourcesSuffix.length; i++) {
				if (uri.endsWith(resourcesSuffix[i])) {
					isResource = true;
					break;
				}
			}
		}
		if (isResource) {
			chain.doFilter(request, resp);
		} else {
			eoapp.handleWebRequest(processedRequest, response);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		String param = (config == null ? null : config.getInitParameter("PizzaConfigFile"));
		String file = null;
		if (param != null && (param = param.trim()).length() > 0) {
			file = param;
		}
		try {
			if (file != null) {
				System.out.println("EoappServletFilter：\n\t" + Pizza.KEY_CONFIG_FILE + "=" + file);
				System.setProperty(Pizza.KEY_CONFIG_FILE, file);
			}

			eoapp = EoApp.getInstance();
			eoapp.setServletContext(config.getServletContext());
			eoapp.startup();
			// ----------------------------------------------------
			String resourcesSuffix = (config == null ? null : config.getInitParameter("resourcesSuffix"));
			if (resourcesSuffix == null || resourcesSuffix.length() == 0) {
				resourcesSuffix = eoapp.getConfigProperties().getProperty(EoAppConstants.KEY_RESOURCES_SUFFIX);
			}
			if (resourcesSuffix != null && resourcesSuffix.length() > 0) {
				Set<String> set = EroicaConfigUtils.split(resourcesSuffix);
				this.resourcesSuffix = new String[set.size()];
				set.toArray(this.resourcesSuffix);
			}
			//
		} finally {
			if (file != null) {
				System.clearProperty(Pizza.KEY_CONFIG_FILE);
			}
		}
	}

}
