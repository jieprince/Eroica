package com.sendtomoon.eroica.fling.monitor.dtos;

public class MonitorMsgDTO implements java.io.Serializable {

	public static final String EVENT_HEARTBEAT = "heartbeat";

	public static final String EVENT_STARTUP = "startup";

	public static final String EVENT_SHUTDOWN = "shutdown";

	public static final String EVENT_SAR_STARTUP = "sar_startup";

	public static final String EVENT_SAR_STARTUP_FAILURE = "sar_startup_failure";

	public static final String EVENT_SAR_SHUTDOWN = "sar_shutdown";

	public static final String EVENT_ERROR = "error";

	/**
	 */
	private static final long serialVersionUID = 1L;

	private String msgId;

	/***
	 * eoapp Name
	 */
	private String appName;

	private String projectId;

	private String domainId;

	private String sarName;

	/** 实例IP */
	private String instanceIp;

	private String activeSars;

	/** 实例名 */
	private String instanceName;

	private InstanceInfoDTO instanceInfo;

	private InstanceStatusDTO instanceStatus;

	private boolean includeInstanceInfo;

	private boolean includeStatusInfo;

	/** 事件：startup/shutdown/heartbeat */
	private String eventName;

	private long eventTimestamp;

	private String bizRequestId;

	private String discription;

	public MonitorMsgDTO() {

	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("{msgId=").append(msgId);
		str.append(",appName=").append(appName);
		str.append(",activeSars=").append(activeSars);
		str.append(",sarName=").append(sarName);
		str.append(",instanceIp=").append(instanceIp);
		str.append(",instanceName=").append(instanceName);
		str.append(",instanceInfo=").append(instanceInfo);
		str.append(",instanceStatus=").append(instanceStatus);
		str.append(",eventName=").append(eventName);
		str.append(",bizRequestId=").append(bizRequestId);
		str.append(",projectId=").append(projectId);
		str.append(",domainId=").append(domainId);
		str.append("}");
		return str.toString();
	}

	public MonitorMsgDTO(String eventName, boolean includeInstanceInfo, boolean includeStatusInfo) {
		this.eventName = eventName;
		this.includeInstanceInfo = includeInstanceInfo;
		this.includeStatusInfo = includeStatusInfo;
		this.eventTimestamp = System.currentTimeMillis();
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getInstanceIp() {
		return instanceIp;
	}

	public void setInstanceIp(String instanceIp) {
		this.instanceIp = instanceIp;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public InstanceInfoDTO getInstanceInfo() {
		return instanceInfo;
	}

	public void setInstanceInfo(InstanceInfoDTO instanceInfo) {
		this.instanceInfo = instanceInfo;
	}

	public InstanceStatusDTO getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(InstanceStatusDTO instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	public boolean isIncludeInstanceInfo() {
		return includeInstanceInfo;
	}

	public void setIncludeInstanceInfo(boolean includeInstanceInfo) {
		this.includeInstanceInfo = includeInstanceInfo;
	}

	public boolean isIncludeStatusInfo() {
		return includeStatusInfo;
	}

	public void setIncludeStatusInfo(boolean includeStatusInfo) {
		this.includeStatusInfo = includeStatusInfo;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public long getEventTimestamp() {
		return eventTimestamp;
	}

	public void setEventTimestamp(long eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getSarName() {
		return sarName;
	}

	public void setSarName(String sarName) {
		this.sarName = sarName;
	}

	public String getBizRequestId() {
		return bizRequestId;
	}

	public void setBizRequestId(String bizRequestId) {
		this.bizRequestId = bizRequestId;
	}

	public String getDiscription() {
		return discription;
	}

	public void setDiscription(String discription) {
		this.discription = discription;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getActiveSars() {
		return activeSars;
	}

	public void setActiveSars(String activeSars) {
		this.activeSars = activeSars;
	}

}
