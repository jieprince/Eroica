package com.sendtomoon.eroica.datasource.monitor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.config.ReferenceConfig;

/**
 * Druid数据库SQL上报
 * 
 */
public class DruidSqlMessagePublisher {

	private static Logger logger = LoggerFactory.getLogger(DruidSqlMessagePublisher.class);

	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	private DruidSqlMonitorDubboService dubboService;

	private ReferenceConfig<DruidSqlMonitorDubboService> reference;

	private final ConcurrentLinkedQueue<DruidSqlMessageDTO> messagesQueue = new ConcurrentLinkedQueue<DruidSqlMessageDTO>();

	private int maxQueueSize = 500;

	public DruidSqlMessagePublisher() {
		init();
	}

	protected void init() {
		PublishThread publishThread = new PublishThread();
		scheduledThreadPool.scheduleAtFixedRate(publishThread, 1L, 10L, TimeUnit.SECONDS);

		reference = new ReferenceConfig<DruidSqlMonitorDubboService>();
		reference.setInterface(DruidSqlMonitorDubboService.class);
		reference.setAsync(true);
		reference.setRetries(0);
		reference.setTimeout(3000);

		dubboService = reference.get();
	}

	public void shutdown() {
		scheduledThreadPool.shutdown();

		if (reference != null) {
			reference.destroy();
			reference = null;
		}
		if (messagesQueue != null) {
			messagesQueue.clear();
		}
	}

	public void pushSqlMsg(DruidSqlMessageDTO msg) {
		if (messagesQueue.size() > maxQueueSize) {
			logger.warn("★★★★★Queue size > " + maxQueueSize);
			return;
		}
		messagesQueue.add(msg);
	}

	public void sendSqlMsg(DruidSqlMessageDTO msg) {
		if (logger.isDebugEnabled()) {
			logger.debug("Send druid sql message for identity:{} and content={}", msg.getIdentity(), msg);
		}

		dubboService.sendSqlMsg(msg);
	}

	private class PublishThread extends Thread {

		public PublishThread() {
			super.setDaemon(true);
			super.setName("t_druid_sql_message_publisher");
		}

		@Override
		public void run() {
			try {
				DruidSqlMessageDTO msg = null;
				while ((msg = messagesQueue.poll()) != null) {
					sendSqlMsg(msg);
				}
			} catch (Throwable t) {
				logger.warn("Failed to send druid sql messages , cause: " + t.getMessage(), t);
			}
		}

	}

}
