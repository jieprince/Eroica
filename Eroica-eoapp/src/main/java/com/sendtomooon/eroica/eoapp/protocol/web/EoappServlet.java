package com.sendtomooon.eroica.eoapp.protocol.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pingan.pafa.pizza.Pizza;
import com.sendtomooon.eroica.eoapp.EoApp;

public class EoappServlet implements javax.servlet.Servlet{
	
	private EoApp papp;

	@Override
	public void destroy() {
		if(papp!=null)
			papp.shutdown();
	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}
	

	@Override
	public String getServletInfo() {
		return "";
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		String param=(config==null?null:config.getInitParameter("PizzaConfigFile"));
		String file=null;
		if(param!=null && (param=param.trim()).length()>0){
			file=param;
		}
		try{
			if(file!=null){
				System.out.println("PappServletFilter：\n\t"+Pizza.KEY_CONFIG_FILE+"="+file);
				System.setProperty(Pizza.KEY_CONFIG_FILE, file);
			}
			/*try {
				URL url=config.getServletContext().getResource("/WEB-INF/papp-lib");
				File libDir=new File(url.toURI());
				if(libDir.exists()){
					System.setProperty(PAppConstants.KEY_LIB_DIR, libDir.getAbsolutePath());
				}
			} catch (Exception e) {
				throw new ServletException(e.getMessage(),e);
			}*/
			papp=EoApp.getInstance();
			papp.setServletContext(config.getServletContext());
			papp.startup();
		}finally{
			if(file!=null){
				System.clearProperty(Pizza.KEY_CONFIG_FILE);
			}
		}
	}

	@Override
	public void service(ServletRequest request, ServletResponse resp)
			throws ServletException, IOException {
		HttpServletRequest processedRequest = (HttpServletRequest)request;
		HttpServletResponse  response=(HttpServletResponse)resp;
		papp.handleWebRequest(processedRequest, response);
	}

}
