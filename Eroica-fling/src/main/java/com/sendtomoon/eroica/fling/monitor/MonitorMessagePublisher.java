package com.sendtomoon.eroica.fling.monitor;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.sendtomoon.eroica.common.utils.InstanceSystemPropertyUtils;
import com.sendtomoon.eroica.common.utils.PNetUtils;
import com.sendtomoon.eroica.fling.monitor.dtos.InstanceInfoDTO;
import com.sendtomoon.eroica.fling.monitor.dtos.MonitorMsgDTO;
import com.sendtomoon.eroica.fling.monitor.services.IMonitorCollectService;
import com.sendtomoon.eroica.fling.monitor.services.MonitorCollectServiceImpl;
import com.sendtomoon.eroica.pizza.PizzaConstants;
import com.sendtomoon.eroica.common.utils.EroicaMeta;

public class MonitorMessagePublisher implements DisposableBean {

	private Log logger = LogFactory.getLog(this.getClass());

	private PublishThread publishThread;

	private FlingMonitorDubboServices dubboServices;

	private ReferenceConfig<FlingMonitorDubboServices> reference;

	private IMonitorCollectService monitorCollectService = new MonitorCollectServiceImpl();

	private final ConcurrentLinkedQueue<MonitorMsgDTO> messagesQueue = new ConcurrentLinkedQueue<MonitorMsgDTO>();

	private int maxQueueSize = 200;

	public MonitorMessagePublisher() {

		init();
	}

	protected void init() {
		publishThread = new PublishThread();
		publishThread.start();
		// ---------------
		reference = new ReferenceConfig<FlingMonitorDubboServices>();
		reference.setInterface(FlingMonitorDubboServices.class);
		reference.setAsync(true);
		reference.setRetries(0);
		reference.setTimeout(3000);
	}

	@Override
	public void destroy() throws Exception {
		shutdown();
	}

	public void pushMonitorMsg(MonitorMsgDTO msg) {
		if (messagesQueue.size() > maxQueueSize) {
			logger.warn("Queue size>" + maxQueueSize);
			return;
		}
		messagesQueue.add(msg);
	}

	public void sendMonitorMsg(MonitorMsgDTO msg) {
		if (dubboServices == null && reference != null) {
			dubboServices = reference.get();
		}
		if (dubboServices == null) {
			return;
		}
		msg.setInstanceIp(PNetUtils.getLocalHost());
		msg.setInstanceName(InstanceSystemPropertyUtils.getInstanceName());
		msg.setProjectId(System.getProperty(PizzaConstants.KEY_PROJECT_ID));
		msg.setDomainId(System.getProperty(PizzaConstants.KEY_DOMAIN_ID));
		// ------------------------------------------
		if (msg.isIncludeInstanceInfo()) {
			InstanceInfoDTO instanceInfo = new InstanceInfoDTO(System.getProperties());
			instanceInfo.setAvailableMemory(monitorCollectService.getAvailableMemory());
			instanceInfo.setAvailableProcessors(monitorCollectService.getAvailableProcessors());
			instanceInfo.setPappVersion(EroicaMeta.VERSION);
			msg.setInstanceInfo(instanceInfo);
		}
		// ---------------------------------------------
		if (msg.isIncludeStatusInfo()) {
			msg.setInstanceStatus(monitorCollectService.getInstanceStatus());
		}
		String appName = msg.getAppName();
		// -------------------------
		if (logger.isInfoEnabled()) {
			logger.info("Send monitor message for app:" + appName + ", instanceIp=" + msg.getInstanceIp()
					+ ", and content=" + msg);
		}
		dubboServices.sendMonitorMsg(msg);
	}

	public void shutdown() {
		if (this.publishThread != null) {
			PublishThread thread = this.publishThread;
			this.publishThread = null;
			thread.shutdown();
		}
		if (reference != null) {
			reference.destroy();
			reference = null;
		}
		if (messagesQueue != null)
			messagesQueue.clear();
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public IMonitorCollectService getMonitorCollectService() {
		return monitorCollectService;
	}

	public void setMonitorCollectService(IMonitorCollectService monitorCollectService) {
		this.monitorCollectService = monitorCollectService;
	}

	private class PublishThread extends Thread {

		private volatile boolean running = true;

		public PublishThread() {
			super.setDaemon(true);
			super.setName("t_fling_monitor_publisher");
		}

		@Override
		public void run() {
			try {
				Thread.sleep(10000);// 延尽启动
			} catch (InterruptedException e) {
			}
			while (running) {
				try {
					MonitorMsgDTO msg = null;
					while (running && (msg = messagesQueue.poll()) != null) {
						sendMonitorMsg(msg);
					}
				} catch (Throwable t) {
					logger.warn("Failed to send fling monitor messages , cause: " + t.getMessage());
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		public void shutdown() {
			running = false;
		}

	}

}
