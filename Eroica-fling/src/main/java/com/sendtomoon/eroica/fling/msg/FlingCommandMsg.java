package com.sendtomoon.eroica.fling.msg;

import java.util.List;

public class FlingCommandMsg {
	
	public static final String TARGET_TYPE_PAPP="papp";
	
	public static final String TARGET_TYPE_SAR="sar";
	
	public static final String ACTION_TYPE_SHUTDOWN="shutdown";
	
	public static final String ACTION_TYPE_UNEXPORT_ESA="unexportESA";
	
	public static final String ACTION_TYPE_STARTUP="startup";
	
	public static final String ACTION_TYPE_RESTARTUP="restartup";
	
	public static final String ACTION_TYPE_ECHO="echo";
	
	//papp/sar
	private String targetType;
	
	//shutdown/startup/restartup
	private String actionType;
	
	//----流水ID
	private String rid;
	
	private String projectId;
	
	private String domainId;
	
	private String pappName;
	
	private String sarName;
	
	private long timestamp;
	
	private List<String> limitIps;

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getPappName() {
		return pappName;
	}

	public void setPappName(String pappName) {
		this.pappName = pappName;
	}

	public String getSarName() {
		return sarName;
	}

	public void setSarName(String sarName) {
		this.sarName = sarName;
	}

	public List<String> getLimitIps() {
		return limitIps;
	}

	public void setLimitIps(List<String> limitIps) {
		this.limitIps = limitIps;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	
	
}
