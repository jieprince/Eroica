package com.sendtomoon.eroica.eoapp.sar;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerLocal;
import com.sendtomoon.eroica.common.app.dto.ServiceRequest;
import com.sendtomoon.eroica.common.app.dto.ServiceResponse;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.pizza.classloader.ClassLoaderStack;
import com.sendtomoon.eroica.pizza.classloader.PizzaClassLoader;
import com.sendtomoon.eroica.eoapp.sar.context.SARSpringContextFactory;
import com.sendtomoon.eroica.eoapp.sar.event.SARShutdownEvent;
import com.sendtomoon.eroica.eoapp.sar.event.SARStartupedEvent;
import com.sendtomoon.eroica.eoapp.web.WebDispatcher;

public final class SARContextBean implements SARContext, Ordered {

	private String sarName;

	private volatile SARContextAttrs contextAttrs;

	private volatile ConfigurableApplicationContext springContext;

	private volatile SARDispatcher dispatcher;

	private volatile PizzaClassLoader classLoader;

	private volatile ConfigurableApplicationContext eoappSpringContext;

	private volatile boolean running;

	private volatile WebDispatcher webDispatcher;

	private volatile ServletContext servletContext;

	private final Lock lock = new ReentrantLock();

	private Log logger = LogFactory.getLog(this.getClass());

	public SARContextBean(SARContextAttrs contextAttrs, PizzaClassLoader classLoader,
			ConfigurableApplicationContext eoappSpringContext, ServletContext servletContext) {
		this.contextAttrs = contextAttrs;
		this.sarName = contextAttrs.getSarName();
		this.eoappSpringContext = eoappSpringContext;
		this.classLoader = classLoader;
		this.servletContext = servletContext;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public String getSARName() {
		return sarName;
	}

	private boolean initSARSpringContext(String sarName) {
		try {
			SARSpringContextFactory context = new SARSpringContextFactory();
			ConfigurableApplicationContext springContext = context.create(eoappSpringContext, classLoader, contextAttrs,
					servletContext);
			this.springContext = springContext;
			this.dispatcher = context.getDispatcher();
			this.webDispatcher = context.getWebDispatcher();
			return true;
		} catch (Exception e) {
			logger.error("Error startup SAR:" + e, e);
			return false;
		}

	}

	private boolean publishSarEvent() {
		try {
			SARStartupedEvent event = new SARStartupedEvent(this);
			springContext.publishEvent(event);
			return true;
		} catch (Throwable e) {
			logger.error("Error publishSarEvent SAR:" + e, e);
			return false;
		}
	}

	@Override
	public synchronized boolean startup() {
		String sarName = this.sarName;
		lock.lock();
		try {
			if (running) {
				throw new SARException("SAR:" + sarName + " be running.");
			}
			if (this.logger.isInfoEnabled()) {
				logger.info("SAR<" + sarName + ">startup...");
			}
			long t1 = System.nanoTime();
			ClassLoader classLoader = this.classLoader;
			if (classLoader == null) {
				throw new NullPointerException("classLoader is null");
			}
			Thread.currentThread().setContextClassLoader(classLoader);
			if (!initSARSpringContext(sarName)) {
				return false;
			}
			if (!publishSarEvent()) {
				return false;
			}
			if (this.logger.isInfoEnabled()) {
				logger.info("SAR<" + sarName + ">startup completed, times=" + (System.nanoTime() - t1) / 1000 / 1000.0
						+ "ms.");
			}
			running = true;
		} catch (BeanCreationException e) {
			SARException ex = new SARException(e.getMessage(), e);
			logger.error("SAR<" + sarName + ">startup failed,cause:\n" + e.getLocalizedMessage(), e);
			running = false;
		} catch (Throwable e) {
			logger.error("SAR<" + sarName + ">startup failed,cause:\n" + e.getLocalizedMessage(), e);
			running = false;
		} finally {
			lock.unlock();
		}

		return running;
	}

	public synchronized void shutdown() {
		lock.lock();
		try {
			if (running) {
				running = false;
				doShutdown();
			}
		} catch (Throwable th) {
			logger.error("SAR<" + sarName + ">shutdown failed,cause:\n" + th.getLocalizedMessage(), th);
		} finally {
			running = false;
			lock.unlock();
		}
	}

	@Override
	public Collection<ESADefinition> getESADefinitions() {
		SARDispatcher dispatcher = this.dispatcher;
		if (dispatcher != null) {
			return dispatcher.getESADefinitions();
		}
		return null;
	}

	protected synchronized final void doShutdown() {
		String sarName = this.sarName;
		ConfigurableApplicationContext springContext = this.springContext;
		if (springContext == null) {
			logger.error("SAR<" + sarName + ">be shutdowned.");
			return;
		} else {
			this.springContext = null;
			this.running = false;
		}
		if (this.logger.isInfoEnabled()) {
			logger.info("SAR<" + sarName + ">shutdown now...");
		}
		try {
			SARShutdownEvent event = new SARShutdownEvent(this);
			springContext.publishEvent(event);
		} catch (Throwable th) {
			logger.error("SAR<" + sarName + ">publishShutdownEvent error,cause:\n" + th.getLocalizedMessage(), th);
		}
		try {
			springContext.close();
		} catch (Throwable th) {
			logger.error("SAR<" + sarName + ">shutdown error,cause:\n" + th.getLocalizedMessage(), th);
		}
		this.webDispatcher = null;
		this.dispatcher = null;
		this.eoappSpringContext = null;
		PizzaClassLoader cl = this.classLoader;
		try {
			cl.close();
		} catch (Exception e) {
		}
		this.classLoader = null;
		if (this.logger.isInfoEnabled()) {
			logger.info("SAR<" + sarName + ">shutdown completed.");
		}
	}

	@Override
	public boolean handleWebRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (!running) {
			return false;
		}
		WebDispatcher webDispatcher = this.webDispatcher;
		if (webDispatcher == null) {
			return false;
		}
		ClassLoader classLoader = this.classLoader;
		try {
			if (classLoader != null) {
				ClassLoaderStack.push(classLoader);
			}
			SARContextAttrs contextAttrs = this.contextAttrs;
			String[] patterns = contextAttrs == null ? null : contextAttrs.getWebPatterns();
			return webDispatcher.handleRequest(request, response, patterns);
		} finally {
			if (classLoader != null) {
				ClassLoaderStack.pop();
			}
		}
	}

	@Override
	public ServiceResponse handleRequest(ServiceRequest request) {
		if (!running) {
			throw new SARException("SAR<" + sarName + "> shutdowned.");
		}
		ClassLoader classLoader = this.classLoader;
		try {
			if (classLoader != null) {
				ClassLoaderStack.push(classLoader);
			}
			return this.getDispatcher().handleRequest(request);
		} finally {
			if (classLoader != null) {
				ClassLoaderStack.pop();
			}
		}
	}

	public ConfigurableApplicationContext getSpringContext() {
		return springContext;
	}

	public ApplicationControllerLocal getDispatcher() {
		return dispatcher;
	}

	public WebDispatcher getWebDispatcher() {
		return webDispatcher;
	}

	@Override
	public int getOrder() {
		return this.contextAttrs.getOrder();
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

}
