package com.sendtomoon.eroica.fling.receipt;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.sendtomoon.eroica.fling.msg.FlingReceiptMsg;

public class CommandReceiptPush implements DisposableBean {
	protected Log logger = LogFactory.getLog(CommandReceiptPush.class);

	private CommandReceiptService receiptService;

	private ReferenceConfig<CommandReceiptService> reference;

	private final Lock lock = new ReentrantLock();

	@Override
	public void destroy() throws Exception {
		logger.info("destroy CommandReceiptService 。。。");
		ReferenceConfig<CommandReceiptService> reference = this.reference;
		this.reference = null;
		if (reference != null) {
			reference.destroy();
		}

		if (receiptService != null) {
			receiptService = null;
		}
	}

	public synchronized void afterPropertiesSet() throws Exception {
		try {
			lock.lock();
			reference = new ReferenceConfig<CommandReceiptService>();
			reference.setInterface(CommandReceiptService.class);
			reference.setAsync(true);
			reference.setScope("remote");
			reference.setCheck(false);
			reference.setRetries(0);
			reference.setTimeout(5000);

			receiptService = reference.get();
		} finally {
			lock.unlock();
		}
	}

	public void receipt(FlingReceiptMsg receiptMsg) {
		if (receiptService == null) {
			try {
				this.afterPropertiesSet();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		receiptService.receipt(receiptMsg);
		logger.info("send CommandReceiptService 。。。success");
	}
}
