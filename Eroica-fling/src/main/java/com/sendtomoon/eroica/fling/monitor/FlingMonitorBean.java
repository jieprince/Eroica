package com.sendtomoon.eroica.fling.monitor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomoon.eroica.eoapp.context.support.SARManager;
import com.sendtomoon.eroica.eoapp.sar.SARContext;
import com.sendtomoon.eroica.fling.monitor.dtos.MonitorMsgDTO;

public class FlingMonitorBean implements FlingMonitor, DisposableBean {

	protected Log logger = LogFactory.getLog(this.getClass());

	private String appName;

	private MonitorMessagePublisher publisher;

	private HeartbeatThread heartbeatThread;

	/** 是否上报instance状态信息 */
	private boolean reportStatus = false;

	private int heartbeatInterval = 60000;

	private boolean enable = false;

	private final Lock lock = new ReentrantLock();

	private SARManager sarManager;

	private boolean pappStartuped = false;

	public FlingMonitorBean() {
	}

	public FlingMonitorBean(String appName) {
		this.appName = appName;
	}

	public void shutdown() {
		try {
			lock.lock();
			if (publisher != null) {
				publisher.shutdown();
				publisher = null;
			}
			if (this.heartbeatThread != null) {
				HeartbeatThread thread = this.heartbeatThread;
				this.heartbeatThread = null;
				thread.shutdown();
			}
			pappStartuped = false;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			lock.unlock();
		}

	}

	protected String generateMsgId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	protected String report(String eventName, boolean reportInstanceInfo, boolean reportStatusInfo, String sarName,
			String discription) {
		if (!enable || !pappStartuped) {
			return null;
		}

		MonitorMsgDTO msg = new MonitorMsgDTO(eventName, reportInstanceInfo, reportStatusInfo);
		msg.setAppName(appName);
		msg.setSarName(sarName);
		String msgId = generateMsgId();
		msg.setMsgId(msgId);
		msg.setBizRequestId(MDCUtil.peek().getRequestId());
		msg.setDiscription(discription);
		if (MonitorMsgDTO.EVENT_STARTUP.equals(eventName) || MonitorMsgDTO.EVENT_HEARTBEAT.equals(eventName)) {
			msg.setActiveSars(getActiveSars());
		}
		// ------------------------------
		if (publisher == null) {
			try {
				lock.lock();
				publisher = new MonitorMessagePublisher();
			} finally {
				lock.unlock();
			}
		}
		// -----------------------
		publisher.pushMonitorMsg(msg);
		return msgId;
	}

	private String getActiveSars() {
		List<SARContext> sarContexts = sarManager.listSARContext();
		if (sarContexts != null && sarContexts.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < sarContexts.size(); i++) {
				SARContext sarContext = sarContexts.get(i);
				if (i == (sarContexts.size() - 1)) {
					sb.append(sarContext.getSARName());
				} else {
					sb.append(sarContext.getSARName()).append("||");
				}
			}
			return sb.toString();
		}
		return new String();
	}

	@Override
	public void destroy() throws Exception {
		shutdown();
	}

	public void init() {
		if (enable && heartbeatThread == null) {
			pappStartuped = true;
			heartbeatThread = new HeartbeatThread();
			heartbeatThread.start();
			this.report(MonitorMsgDTO.EVENT_STARTUP, true, reportStatus, null, "Papp startup.");
		}
	}

	@Override
	public String reportForSARStartupFailed(String sarName) {
		return this.report(MonitorMsgDTO.EVENT_SAR_STARTUP_FAILURE, false, false, sarName, null);
	}

	@Override
	public String reportForSARStoped(String sarName) {
		return this.report(MonitorMsgDTO.EVENT_SAR_SHUTDOWN, false, false, sarName, null);
	}

	@Override
	public String reportForSARStartuped(String sarName) {
		return this.report(MonitorMsgDTO.EVENT_SAR_STARTUP, false, false, sarName, null);
	}

	protected String exceptionToString(String msg, Throwable th) {
		if (th == null) {
			return null;
		}
		if (msg == null) {
			msg = th.getMessage();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
		PrintStream out = new PrintStream(bos);
		out.println(msg);
		th.printStackTrace(out);
		return new String(bos.toByteArray());
	}

	@Override
	public String reportError(Throwable thrown, String sarName) {
		return this.report(MonitorMsgDTO.EVENT_ERROR, false, false, sarName, exceptionToString(null, thrown));
	}

	@Override
	public String reportError(Throwable thrown) {
		return this.report(MonitorMsgDTO.EVENT_ERROR, false, false, null, exceptionToString(null, thrown));
	}

	@Override
	public String reportError(String msg, Throwable thrown, String sarName) {
		return this.report(MonitorMsgDTO.EVENT_ERROR, false, false, sarName, exceptionToString(msg, thrown));
	}

	@Override
	public String reportError(String msg, Throwable thrown) {
		return this.report(MonitorMsgDTO.EVENT_ERROR, false, false, null, exceptionToString(msg, thrown));
	}

	/*
	 * public String reportForSAR(String eventName, String sarName) { MonitorMsgDTO
	 * msg=new MonitorMsgDTO(eventName,false,false); msg.setAppName(appName);
	 * msg.setSarName(sarName); String msgId=generateMsgId(); msg.setMsgId(msgId);
	 * msg.setBizRequestId(MDCUtil.peek().getRequestId());
	 * //------------------------------ if(publisher==null){ try { lock.lock();
	 * publisher=new MonitorMessagePublisher(); } finally { lock.unlock(); } }
	 * //----------------------- publisher.pushMonitorMsg(msg); return msgId; }
	 */

	private class HeartbeatThread extends Thread {
		private volatile boolean running = true;

		public HeartbeatThread() {
			super.setDaemon(true);
			super.setName("t_fling_monitor_heartbeat");
		}

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(heartbeatInterval);
				} catch (InterruptedException e) {
				}
				try {
					report(MonitorMsgDTO.EVENT_HEARTBEAT, false, reportStatus, null, "papp heartbeat.");
				} catch (Throwable t) {
					logger.warn("Failed to send fling monitor messages , cause: " + t.getMessage());
				}
			}
		}

		public void shutdown() {
			running = false;
		}
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public MonitorMessagePublisher getPublisher() {
		return publisher;
	}

	public void setPublisher(MonitorMessagePublisher publisher) {
		this.publisher = publisher;
	}

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(int heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public boolean isReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(boolean reportStatus) {
		this.reportStatus = reportStatus;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public SARManager getSarManager() {
		return sarManager;
	}

	public void setSarManager(SARManager sarManager) {
		this.sarManager = sarManager;
	}

	public boolean isPappStartuped() {
		return pappStartuped;
	}

	public void setPappStartuped(boolean pappStartuped) {
		this.pappStartuped = pappStartuped;
	}

}
