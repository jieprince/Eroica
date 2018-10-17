
package com.sendtomoon.eroica.eoapp.protocol.jetty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import com.sendtomoon.eroica.common.utils.NetPortUtils;
import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;
import com.sendtomoon.eroica.eoapp.EoAppException;
import com.sendtomoon.eroica.eoapp.context.config.EoAppConstants;
import com.sendtomoon.eroica.eoapp.protocol.web.EoappServlet;

public class JettyProtocolBean implements SmartLifecycle, JettyProtocol, BeanClassLoaderAware {

	private Log logger = LogFactory.getLog(JettyProtocolBean.class);

	private JettyAttrs attrs;

	private ServletContextHandler context;

	private String appName;

	private Server server;

	private Resource configureResource;

	private Properties configureProperties;

	private boolean autoStartup = true;

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return autoStartup;
	}

	@Override
	public void stop(Runnable command) {
		stop();
		command.run();
	}

	@PostConstruct
	public void init() {
		start();
	}

	public synchronized void start() {
		if (this.isRunning()) {
			return;
		}
		if (attrs == null) {
			attrs = new JettyAttrs();
		}
		Properties mergProperties = new Properties();
		if (this.configureProperties != null) {
			mergProperties.putAll(this.configureProperties);
		}
		if (configureResource != null && configureResource.exists()) {
			InputStream input = null;
			try {
				input = configureResource.getInputStream();
				mergProperties.load(input);
			} catch (IOException ex) {
				throw new FatalBeanException(
						"Read configure resource:" + configureResource + " error,cause:" + ex.getMessage(), ex);
			} finally {
				try {
					if (input != null)
						input.close();
				} catch (IOException e) {
				}
			}
		}
		Properties properties = new Properties();
		if (mergProperties != null && mergProperties.size() > 0) {
			Enumeration<Object> keys = mergProperties.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				if (key.startsWith(JettyAttrs.PROPERTIES_PREFIX)) {
					properties.setProperty(key.substring(JettyAttrs.PROPERTIES_PREFIX.length()),
							mergProperties.getProperty(key));
				}
			}
		}
		if (properties.size() > 0) {
			EroicaConfigUtils.bindBean(properties, attrs);
		}
		// ------------------------
		String webroot = System.getProperty(EoAppConstants.KEY_WEB_ROOT);
		if (webroot == null || webroot.length() == 0) {
			webroot = attrs.getWebroot();
		}
		if (webroot != null && (webroot = webroot.trim()).length() > 0) {
			try {
				File dir = ResourceUtils.getFile(webroot);
				if (!dir.isDirectory() || !dir.exists()) {
					throw new FileNotFoundException("Webroot<" + webroot + "> not exists or not be Directory.");
				}
				attrs.setWebrootDirectory(dir.getAbsolutePath());
				attrs.setWebroot(webroot);
			} catch (Throwable e) {
				throw new EoAppException("Jetty inited error ,cause:" + e.getMessage(), e);
			}
		} else {
			attrs.setWebroot(null);
		}
		// --------------------------------------
		try {
			server = new Server(attrs.getPort());
			startServer(attrs);
		} catch (Throwable e) {
			throw new EoAppException("Jetty statup failed,cause:" + e.getMessage(), e);
		}
	}

	protected void startServer(JettyAttrs attrs) {
		if (NetPortUtils.isLoclePortUsing(attrs.getPort())) {
			throw new IllegalArgumentException("Port<" + attrs.getPort() + "> be used.");
		}
		context = new ServletContextHandler(server, attrs.getContextPath());
		context.setSessionHandler(new SessionHandler());
		context.setAttribute(JettyAttrs.ATTR_KEY, attrs);
		if (attrs.getMaxFormContentSize() != -1) {
			context.setMaxFormContentSize(attrs.getMaxFormContentSize());
		}
		if (attrs.getMaxFormKeys() != -1) {
			context.setMaxFormKeys(attrs.getMaxFormKeys());
		}
		// -------
		ClassLoader classLoader = this.classLoader;
		if (classLoader == null) {
			classLoader = ClassUtils.getDefaultClassLoader();
		}
		if (classLoader != null) {
			context.setClassLoader(classLoader);
		}
		if (attrs.getWebrootDirectory() != null) {
			context.setResourceBase(attrs.getWebrootDirectory());
		}
		context.addFilter(JettyResourceFilter.class, "/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.FORWARD,
				DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR));
		context.addServlet(EoappServlet.class, "/*");
		if (attrs.isWebsocketEnable()) {
			try {
				Class<?> websocketInitClazz = ClassUtils.forName(
						"org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer",
						classLoader);
				Method initMethod = websocketInitClazz.getMethod("configureContext", ServletContextHandler.class);
				initMethod.invoke(null, context);
			} catch (Exception ex) {
				throw new FatalBeanException(
						"Init websocket container failure by classLoader=" + classLoader + ",cause:" + ex.getMessage(),
						ex);
			}
		}
		try {
			server.start();
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to start jetty server on port<" + attrs.getPort() + ">, cause: " + e.getMessage(), e);
		}

	}

	@Override
	public boolean isRunning() {
		return server != null && server.isRunning();
	}

	public synchronized void stop() {
		try {
			if (server != null) {
				Server temp = server;
				this.server = null;
				temp.stop();
			}
		} catch (Throwable e) {
			logger.error("Jetty error,cause:" + e.getMessage(), e);
		}

	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public ContextHandler getContext() {
		return context;
	}

	public ServletContext getServletContext() {
		return context == null ? null : context.getServletContext();
	}

	public ServletContext resolveServletContext() {
		return getServletContext();
	}

	public Resource getConfigureResource() {
		return configureResource;
	}

	public void setConfigureResource(Resource configureResource) {
		this.configureResource = configureResource;
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	public Properties getConfigureProperties() {
		return configureProperties;
	}

	public void setConfigureProperties(Properties configureProperties) {
		this.configureProperties = configureProperties;
	}

	public JettyAttrs getAttrs() {
		return attrs;
	}

	public void setAttrs(JettyAttrs attrs) {
		this.attrs = attrs;
	}

	private ClassLoader classLoader;

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

}