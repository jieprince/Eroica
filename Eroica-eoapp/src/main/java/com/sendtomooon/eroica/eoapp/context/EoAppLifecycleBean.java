package com.sendtomooon.eroica.eoapp.context;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.unidal.cat.Cat;
import org.unidal.cat.message.Transaction;

import com.pingan.pafa.pizza.Pizza;
import com.pingan.pafa.pizza.classloader.PizzaClassLoader;
import com.pingan.pafa.pola.Pola;
import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomooon.eroica.eoapp.EoAppException;
import com.sendtomooon.eroica.eoapp.context.config.EoAppConfigProperties;
import com.sendtomooon.eroica.eoapp.context.config.EoAppConstants;
import com.sendtomooon.eroica.eoapp.context.event.EoAppConfigChangedEvent;
import com.sendtomooon.eroica.eoapp.context.event.EoAppShutdownEvent;
import com.sendtomooon.eroica.eoapp.context.event.EoAppStartupedEvent;
import com.sendtomooon.eroica.eoapp.context.lib.EoAppClassLoaderFactory;
import com.sendtomooon.eroica.eoapp.context.support.SARManager;

public class EoAppLifecycleBean
		implements SmartLifecycle, EoAppLifecycle, ApplicationListener<EoAppConfigChangedEvent> {

	private Log logger = LogFactory.getLog(this.getClass());

	private volatile boolean isRunning;

	private volatile boolean startFailed;

	private String eoappName;

	private String sarList;

	private volatile EoAppClassLoaderFactory classLoaderFactory;

	private volatile PizzaClassLoader classLoader;

	private volatile EoAppSpringContextFactory springContextFactory;

	private volatile EoAppSpringContext springContext;

	private volatile EoAppConfigProperties configProperties;

	private Pola pola;

	private ServletContext servletContext;

	private volatile SARManager sarManager;

	private final Lock lock = new ReentrantLock();

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void start() {
		lock.lock();
		try {
			if (isRunning) {
				return;
			}
			startFailed = false;
			doStartup();

		} catch (Throwable ex) {
			startFailed = true;
			String eoappName = this.eoappName;
			if (logger != null) {
				logger.error("EoApp<" + eoappName + "> startup error:" + ex.getMessage(), ex);
			}
			throw new EoAppException("EoApp<" + eoappName + "> startup error:" + ex.getMessage(), ex);
		} finally {
			lock.unlock();
		}
	}

	protected synchronized final boolean doStartup() {
		Transaction t = Cat.newTransaction("Lifcycle.startup", "EoAPP");
		try {
			if (isRunning) {
				return false;
			}
			long t1 = System.nanoTime();

			initClassloader();

			// ----------------------------------------------
			EoAppSpringContext springContext = initSpringContext();

			SARManager sarManager = startupSARs(springContext);
			//
			EoAppStartupedEvent event = new EoAppStartupedEvent(springContext.getApplicationContext());

			springContext.getApplicationContext().publishEvent(event);
			Pizza.getSpringContext().publishEvent(event);
			this.springContext = springContext;
			this.sarManager = sarManager;
			// -----------------------------------------------
			isRunning = true;
			startFailed = false;
			if (logger.isInfoEnabled()) {
				logger.info("EoApp<" + eoappName + "> startup completed, times="
						+ (System.nanoTime() - t1) / 1000 / 1000.0 + "ms.");
			}
			t.setSuccessStatus();
			return isRunning;
		} catch (RuntimeException e) {
			t.setStatus(e);
			throw e;
		} catch (Error e) { // 这个没意义，类似的还有
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
		}
	}

	private SARManager startupSARs(EoAppSpringContext springContext) {
		Transaction t = Cat.newTransaction("Lifcycle.SARStartup", "EoAPP");
		try {

			this.servletContext = springContext.getServletContext();

			String sarList = this.getSarList();
			if (logger.isInfoEnabled()) {
				logger.info("EoApp SARList=" + sarList);
			}
			t.addData("SARList", sarList);
			SARManager sarManager = springContext.getApplicationContext().getBean(SARManager.class);
			sarManager.startupSARs(sarList);
			t.setSuccessStatus();
			return sarManager;
		} catch (RuntimeException e) {
			t.setStatus(e);
			throw e;
		} catch (Error e) {
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
		}
	}

	private EoAppSpringContext initSpringContext() {
		Transaction t = Cat.newTransaction("Lifcycle.ContextInit", "EoAPP");
		try {
			EoAppSpringContextFactory springContextFactory = this.getSpringContextFactory();
			if (springContextFactory == null) {
				throw new NullPointerException("springContextFactory is null.");
			}
			EoAppSpringContext springContext = springContextFactory.create(classLoader, servletContext, this.getPola());
			t.setSuccessStatus();
			return springContext;
		} catch (RuntimeException e) {
			t.setStatus(e);
			throw e;
		} catch (Error e) {
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
		}
	}

	private void initClassloader() {
		Transaction t = Cat.newTransaction("Lifcycle.ClassloaderInit", "EoAPP");
		try {
			if (logger.isInfoEnabled()) {
				logger.info("EoApp<" + eoappName + "> startup,datetime="
						+ DateFormat.getDateTimeInstance().format(new Date()) + ".");
			}
			EoAppClassLoaderFactory classLoaderFactory = this.getClassLoaderFactory();
			if (classLoaderFactory == null) {
				throw new NullPointerException("classLoaderFactory is null.");
			}
			PizzaClassLoader classLoader = classLoaderFactory.getClassLoader();
			if (classLoader != null) {
				Thread.currentThread().setContextClassLoader(classLoader);
			}
			this.classLoader = classLoader;
			if (logger.isInfoEnabled()) {
				logger.info("EoAppClassLoader=" + classLoader);
			}
			t.setSuccessStatus();
		} catch (RuntimeException e) {
			t.setStatus(e);
			throw e;
		} catch (Error e) {
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
		}
	}

	protected synchronized final boolean doShutdown() {
		Transaction t = Cat.newTransaction("EoAPP", "EoAPP.Lifecycle.doShutdown");
		try {
			PizzaClassLoader classLoader = this.classLoader;
			if (classLoader != null) {
				Thread.currentThread().setContextClassLoader(classLoader);
			}
			logger.info("EoApp<" + this.eoappName + "> shutdown now.");
			this.isRunning = false;
			EoAppSpringContext springContext = this.springContext;
			if (springContext == null) {
				logger.warn("EoApp<" + this.eoappName + "> shutdowned.");
				return true;
			}
			ConfigurableApplicationContext applicationContext = springContext.getApplicationContext();
			// -----------------------------------
			sarManager.shutdown();
			// 发布eoapp层shutdown事件
			applicationContext.publishEvent(new EoAppShutdownEvent(applicationContext));
			// 发布eoapp shutdown事件给root层
			Pizza.getSpringContext().publishEvent(new EoAppShutdownEvent(applicationContext));
			if (springContext != null) {
				try {
					springContext.close();
					springContext = null;
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
			if (classLoader != null) {
				Thread.currentThread().setContextClassLoader(null);
				this.classLoader = null;
				try {
					classLoader.close();
				} catch (Exception e) {
				}
			}
			applicationContext = null;
			this.startFailed = false;
			logger.info("EoApp<" + eoappName + "> shutdown completed.");

			t.setSuccessStatus();
			return true;
		} catch (RuntimeException e) {
			t.setStatus(e);
			throw e;
		} catch (Error e) {
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
		}
	}

	public boolean shutdown() {
		Transaction t = Cat.newTransaction("Lifcycle.shutdown", "EoAPP");
		lock.lock();

		t.addData("eoappName", eoappName);
		try {
			if (this.isRunning) {
				boolean b = doShutdown();
				t.setSuccessStatus();
				return b;
			}
			return false;
		} catch (Throwable th) {
			t.setStatus(th);
			Cat.logError(th);
			logger.error("EoAPP<" + eoappName + ">shutdown failed,cause:\n" + th.getLocalizedMessage(), th);
			return false;
		} finally {
			lock.unlock();
			t.complete();
		}
	}

	@Override
	public void stop() {
		shutdown();
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return false;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public EoAppSpringContext getSpringContext() {
		return springContext;
	}

	public boolean isStartFailed() {
		return startFailed;
	}

	public EoAppClassLoaderFactory getClassLoaderFactory() {
		return classLoaderFactory;
	}

	public void setClassLoaderFactory(EoAppClassLoaderFactory classLoaderFactory) {
		this.classLoaderFactory = classLoaderFactory;
	}

	public EoAppSpringContextFactory getSpringContextFactory() {
		return springContextFactory;
	}

	public void setSpringContextFactory(EoAppSpringContextFactory springContextFactory) {
		this.springContextFactory = springContextFactory;
	}

	public String getEoappName() {
		return eoappName;
	}

	public void setEoappName(String eoappName) {
		this.eoappName = eoappName;
	}

	public String getSarList() {
		return sarList;
	}

	public void setSarList(String sarList) {
		this.sarList = sarList;
	}

	public EoAppConfigProperties getConfigProperties() {
		return configProperties;
	}

	public void setConfigProperties(EoAppConfigProperties configProperties) {
		this.configProperties = configProperties;
	}

	@Override
	public void onApplicationEvent(EoAppConfigChangedEvent event) {
		lock.lock();
		try {
			MDCUtil.set();
			EoAppConfigProperties configProperties = event.getConfigProperties();
			if (isRunning) {
				if (classLoader != null) {
					Thread.currentThread().setContextClassLoader(classLoader);
				}
				// -------------------
				sarManager.refreshSARs(configProperties.getProperty(EoAppConstants.KEY_SAR_LIST));
			}
		} catch (Throwable ex) {
			logger.error("Refresh EoApp config error, case:" + ex.getMessage(), ex);
		} finally {
			MDCUtil.clear();
			lock.unlock();
		}
	}

	public Pola getPola() {
		return pola;
	}

	public void setPola(Pola pola) {
		this.pola = pola;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public void setOriginalServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}