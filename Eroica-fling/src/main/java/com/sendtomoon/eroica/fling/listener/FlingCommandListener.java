package com.sendtomoon.eroica.fling.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSONObject;
import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomoon.eroica.fling.FlingCommand;
import com.sendtomoon.eroica.fling.msg.FlingCommandMsg;
import com.sendtomoon.eroica.pizza.PizzaContext;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;
import com.sendtomoon.eroica.pizza.spring.PizzaResourceListener;

public class FlingCommandListener implements PizzaResourceListener, InitializingBean, DisposableBean {

	protected Log logger = LogFactory.getLog(this.getClass());

	private volatile ThreadPoolExecutor threadPoolExecutor;

	private int threadSize = 3;

//	private static final long VALID_TIME = 5 * 60 * 1000;

	private final Lock lock = new ReentrantLock();

//	private volatile String lastRid;

	private FlingCommandHandler commandHandler;

	private boolean enable = true;

	private PizzaContext pizzaContext;

	public FlingCommandListener() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!this.isEnable()) {
			logger.warn("Fling command be disabled,PizzaProperty:fling.cmd.enable=false");
			return;
		}
	}

	@Override
	public void onChanged(PizzaURL pizzaURL, InputStream content) {
		if (content == null) {
			return;
		}
		String commandJson = null;
		try {
			commandJson = IOUtils.toString(content);
		} catch (IOException e) {
		}
		if (logger.isInfoEnabled()) {
			logger.info("Received fling command=" + commandJson);
		}
		handleCommand(commandJson);
	}

	@Override
	public PizzaURL getPizzaURL() {
		return PizzaURL.valueOf("/" + FlingCommand.PIZZA_GROUP_FLING_NAME + "/" + pizzaContext.getAppName());
	}

	public void handleCommand(String commandJson) {
		if (StringUtils.isEmpty(commandJson)) {
			return;
		}
		if (this.commandHandler == null) {
			throw new NullPointerException("commandHandler requried.");
		}
		final FlingCommandMsg cmd = JSONObject.parseObject(commandJson, FlingCommandMsg.class);
		if (threadPoolExecutor == null) {
			try {
				lock.lock();
				if (threadPoolExecutor == null) {
					threadPoolExecutor = new ThreadPoolExecutor(threadSize, threadSize * 3, 60000,
							TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
				}
			} finally {
				lock.unlock();
			}
		}

		threadPoolExecutor.execute(new Runnable() {
			@Override
			public void run() {
				MDCUtil.set();
				try {
					commandHandler.handleCommand(cmd);
				} finally {
					MDCUtil.clear();
				}
			}
		});
	}

	public void destroy() {
		try {
			if (threadPoolExecutor != null) {
				threadPoolExecutor.shutdown();
				threadPoolExecutor = null;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public boolean isListenEnable() {
		return enable;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public int getThreadSize() {
		return threadSize;
	}

	public void setThreadSize(int threadSize) {
		this.threadSize = threadSize;
	}

	public FlingCommandHandler getCommandHandler() {
		return commandHandler;
	}

	public void setCommandHandler(FlingCommandHandler commandHandler) {
		this.commandHandler = commandHandler;
	}

	public PizzaContext getPizzaContext() {
		return pizzaContext;
	}

	public void setPizzaContext(PizzaContext pizzaContext) {
		this.pizzaContext = pizzaContext;
	}

}
