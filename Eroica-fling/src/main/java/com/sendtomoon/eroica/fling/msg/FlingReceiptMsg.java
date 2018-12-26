package com.sendtomoon.eroica.fling.msg;

import java.io.Serializable;

/***
 * 指令执行回执信息Form
 * @author LIXINGNAN945
 *
 */
public class FlingReceiptMsg implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public  static final String RCODE_SUCCESS="0";
	
	public  static final String RCODE_APP_CMD_FAILURE="1";
	
	public  static final String RCODE_SAR_CMD_FAILURE="2";
	
	public  static final String RCODE_FATAL_ERROR="9";
	
	/**
	 * 表示已接收到执行指令
	 */
	public static final String RCODE_APP_CMD_RECEIVED="3";
	
	
	
	//指令流水ID
	private String rid;
	
	//回执者IP地址
	private String instanceIp;
	
	private String instanceName;
	
	//回执行日期
	private long receiptDate;
	
	//执行结果，0成功,非0失败
	private String responseCode;
	
	private String responseMsg;
	
	
	//papp/sar
	private String targetType;
	
	//stop/start/restart
	private String actionType;
	
	/**@deprecated*/
	private String projectId;
	
	private String domainId;
	
	private String pappName;
	
	private String sarName;
	
	private String logId;
	
	private long elapsedTime;

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
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

	public long getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(long receiptDate) {
		this.receiptDate = receiptDate;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
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

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	@Override
	public String toString() {
		return "FlingReceiptMsg [rid=" + rid + ", instanceIp=" + instanceIp + ", instanceName=" + instanceName
				+ ", receiptDate=" + receiptDate + ", responseCode=" + responseCode + ", responseMsg=" + responseMsg
				+ ", targetType=" + targetType + ", actionType=" + actionType + ", projectId=" + projectId
				+ ", domainId=" + domainId + ", pappName=" + pappName + ", sarName=" + sarName + ", logId=" + logId
				+ ", elapsedTime=" + elapsedTime + "]";
	}
}
