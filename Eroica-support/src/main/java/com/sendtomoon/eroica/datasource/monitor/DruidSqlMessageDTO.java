package com.sendtomoon.eroica.datasource.monitor;

import java.util.Arrays;
import java.util.Date;

public class DruidSqlMessageDTO implements java.io.Serializable {
	
	private static final long serialVersionUID = -619322255384477628L;

	private String msgId;
	
	private int identity;
    
    private long sqlId;
    
    private String druidSql;
    
    private long executeCount;
    
    private long errorCount;
    
    private long totalTime;
    
    private Date lastTime;
    
    private long maxTimespan;
    
    private String lastError;
    
    private long effectedRowCount;
    
    private long fetchRowCount;
    
    private Date maxTimespanOccurTime;
    
    private long batchSizeMax;
    
    private long batchSizeTotal;
    
    private long concurrentMax;
    
    private long runningCount;
    
    private String druidName;
    
    private String druidFile;
    
    private String lastErrorMessage;
    
    private String lastErrorClass;
    
    private String lastErrorStackTrace;
    
    private Date lastErrorTime;
    
    private String dbType;
    
    private long inTransactionCount;
    
    private long[] histogram;
    
    private String lastSlowParameters;
    
    private long resultSetHoldTime;
    
    private long executeAndResultSetHoldTime;
    
    private long[] fetchRowCountHistogram;
    
    private long[] effectedRowCountHistogram;
    
    private long[] executeAndResultHoldTimeHistogram;
    
    private long effectedRowCountMax;
    
    private long fetchRowCountMax;
    
    private long clobOpenCount;
    
    private long blobOpenCount;
    
    private long readStringLength;
    
    private long readBytesLength;
    
    private long inputStreamOpenCount;
    
    private long readerOpenCount;
    
    private long sqlHash;

	@Override
	public String toString() {
		return "DruidSqlMessageDTO [msgId=" + msgId + ", identiy=" + identity + ", sqlId=" + sqlId + ", druidSql="
				+ druidSql + ", executeCount=" + executeCount + ", errorCount=" + errorCount + ", totalTime="
				+ totalTime + ", lastTime=" + lastTime + ", maxTimespan=" + maxTimespan + ", lastError=" + lastError
				+ ", effectedRowCount=" + effectedRowCount + ", fetchRowCount=" + fetchRowCount
				+ ", maxTimespanOccurTime=" + maxTimespanOccurTime + ", batchSizeMax=" + batchSizeMax
				+ ", batchSizeTotal=" + batchSizeTotal + ", concurrentMax=" + concurrentMax + ", runningCount="
				+ runningCount + ", druidName=" + druidName + ", druidFile=" + druidFile + ", lastErrorMessage="
				+ lastErrorMessage + ", lastErrorClass=" + lastErrorClass + ", lastErrorStackTrace="
				+ lastErrorStackTrace + ", lastErrorTime=" + lastErrorTime + ", dbType=" + dbType
				+ ", inTransactionCount=" + inTransactionCount + ", histogram=" + Arrays.toString(histogram)
				+ ", lastSlowParameters=" + lastSlowParameters + ", resultSetHoldTime=" + resultSetHoldTime
				+ ", executeAndResultSetHoldTime=" + executeAndResultSetHoldTime + ", fetchRowCountHistogram="
				+ Arrays.toString(fetchRowCountHistogram) + ", effectedRowCountHistogram="
				+ Arrays.toString(effectedRowCountHistogram) + ", executeAndResultHoldTimeHistogram="
				+ Arrays.toString(executeAndResultHoldTimeHistogram) + ", effectedRowCountMax=" + effectedRowCountMax
				+ ", fetchRowCountMax=" + fetchRowCountMax + ", clobOpenCount=" + clobOpenCount + ", blobOpenCount="
				+ blobOpenCount + ", readStringLength=" + readStringLength + ", readBytesLength=" + readBytesLength
				+ ", inputStreamOpenCount=" + inputStreamOpenCount + ", readerOpenCount=" + readerOpenCount
				+ ", sqlHash=" + sqlHash + "]";
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public int getIdentity() {
		return identity;
	}

	public void setIdentity(int identity) {
		this.identity = identity;
	}

	public long getSqlId() {
		return sqlId;
	}

	public void setSqlId(long sqlId) {
		this.sqlId = sqlId;
	}

	public String getDruidSql() {
		return druidSql;
	}

	public void setDruidSql(String druidSql) {
		this.druidSql = druidSql;
	}

	public long getExecuteCount() {
		return executeCount;
	}

	public void setExecuteCount(long executeCount) {
		this.executeCount = executeCount;
	}

	public long getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(long errorCount) {
		this.errorCount = errorCount;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public Date getLastTime() {
		return lastTime;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

	public long getMaxTimespan() {
		return maxTimespan;
	}

	public void setMaxTimespan(long maxTimespan) {
		this.maxTimespan = maxTimespan;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public long getEffectedRowCount() {
		return effectedRowCount;
	}

	public void setEffectedRowCount(long effectedRowCount) {
		this.effectedRowCount = effectedRowCount;
	}

	public long getFetchRowCount() {
		return fetchRowCount;
	}

	public void setFetchRowCount(long fetchRowCount) {
		this.fetchRowCount = fetchRowCount;
	}

	public Date getMaxTimespanOccurTime() {
		return maxTimespanOccurTime;
	}

	public void setMaxTimespanOccurTime(Date maxTimespanOccurTime) {
		this.maxTimespanOccurTime = maxTimespanOccurTime;
	}

	public long getBatchSizeMax() {
		return batchSizeMax;
	}

	public void setBatchSizeMax(long batchSizeMax) {
		this.batchSizeMax = batchSizeMax;
	}

	public long getBatchSizeTotal() {
		return batchSizeTotal;
	}

	public void setBatchSizeTotal(long batchSizeTotal) {
		this.batchSizeTotal = batchSizeTotal;
	}

	public long getConcurrentMax() {
		return concurrentMax;
	}

	public void setConcurrentMax(long concurrentMax) {
		this.concurrentMax = concurrentMax;
	}

	public long getRunningCount() {
		return runningCount;
	}

	public void setRunningCount(long runningCount) {
		this.runningCount = runningCount;
	}

	public String getDruidName() {
		return druidName;
	}

	public void setDruidName(String druidName) {
		this.druidName = druidName;
	}

	public String getDruidFile() {
		return druidFile;
	}

	public void setDruidFile(String druidFile) {
		this.druidFile = druidFile;
	}

	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	public void setLastErrorMessage(String lastErrorMessage) {
		this.lastErrorMessage = lastErrorMessage;
	}

	public String getLastErrorClass() {
		return lastErrorClass;
	}

	public void setLastErrorClass(String lastErrorClass) {
		this.lastErrorClass = lastErrorClass;
	}

	public String getLastErrorStackTrace() {
		return lastErrorStackTrace;
	}

	public void setLastErrorStackTrace(String lastErrorStackTrace) {
		this.lastErrorStackTrace = lastErrorStackTrace;
	}

	public Date getLastErrorTime() {
		return lastErrorTime;
	}

	public void setLastErrorTime(Date lastErrorTime) {
		this.lastErrorTime = lastErrorTime;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public long getInTransactionCount() {
		return inTransactionCount;
	}

	public void setInTransactionCount(long inTransactionCount) {
		this.inTransactionCount = inTransactionCount;
	}

	public long[] getHistogram() {
		return histogram;
	}

	public void setHistogram(long[] histogram) {
		this.histogram = histogram;
	}

	public String getLastSlowParameters() {
		return lastSlowParameters;
	}

	public void setLastSlowParameters(String lastSlowParameters) {
		this.lastSlowParameters = lastSlowParameters;
	}

	public long getResultSetHoldTime() {
		return resultSetHoldTime;
	}

	public void setResultSetHoldTime(long resultSetHoldTime) {
		this.resultSetHoldTime = resultSetHoldTime;
	}

	public long getExecuteAndResultSetHoldTime() {
		return executeAndResultSetHoldTime;
	}

	public void setExecuteAndResultSetHoldTime(long executeAndResultSetHoldTime) {
		this.executeAndResultSetHoldTime = executeAndResultSetHoldTime;
	}

	public long[] getFetchRowCountHistogram() {
		return fetchRowCountHistogram;
	}

	public void setFetchRowCountHistogram(long[] fetchRowCountHistogram) {
		this.fetchRowCountHistogram = fetchRowCountHistogram;
	}

	public long[] getEffectedRowCountHistogram() {
		return effectedRowCountHistogram;
	}

	public void setEffectedRowCountHistogram(long[] effectedRowCountHistogram) {
		this.effectedRowCountHistogram = effectedRowCountHistogram;
	}

	public long[] getExecuteAndResultHoldTimeHistogram() {
		return executeAndResultHoldTimeHistogram;
	}

	public void setExecuteAndResultHoldTimeHistogram(long[] executeAndResultHoldTimeHistogram) {
		this.executeAndResultHoldTimeHistogram = executeAndResultHoldTimeHistogram;
	}

	public long getEffectedRowCountMax() {
		return effectedRowCountMax;
	}

	public void setEffectedRowCountMax(long effectedRowCountMax) {
		this.effectedRowCountMax = effectedRowCountMax;
	}

	public long getFetchRowCountMax() {
		return fetchRowCountMax;
	}

	public void setFetchRowCountMax(long fetchRowCountMax) {
		this.fetchRowCountMax = fetchRowCountMax;
	}

	public long getClobOpenCount() {
		return clobOpenCount;
	}

	public void setClobOpenCount(long clobOpenCount) {
		this.clobOpenCount = clobOpenCount;
	}

	public long getBlobOpenCount() {
		return blobOpenCount;
	}

	public void setBlobOpenCount(long blobOpenCount) {
		this.blobOpenCount = blobOpenCount;
	}

	public long getReadStringLength() {
		return readStringLength;
	}

	public void setReadStringLength(long readStringLength) {
		this.readStringLength = readStringLength;
	}

	public long getReadBytesLength() {
		return readBytesLength;
	}

	public void setReadBytesLength(long readBytesLength) {
		this.readBytesLength = readBytesLength;
	}

	public long getInputStreamOpenCount() {
		return inputStreamOpenCount;
	}

	public void setInputStreamOpenCount(long inputStreamOpenCount) {
		this.inputStreamOpenCount = inputStreamOpenCount;
	}

	public long getReaderOpenCount() {
		return readerOpenCount;
	}

	public void setReaderOpenCount(long readerOpenCount) {
		this.readerOpenCount = readerOpenCount;
	}

	public long getSqlHash() {
		return sqlHash;
	}

	public void setSqlHash(long sqlHash) {
		this.sqlHash = sqlHash;
	}

}
