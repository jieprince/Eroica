package com.sendtomoon.eroica.datasource.monitor;

import java.util.Arrays;
import java.util.Date;

public class DruidStatMessageDTO implements java.io.Serializable {

	private static final long serialVersionUID = 4162235175776375388L;

	private String msgId;

	private String domainId;

	private String eoappName;

	/** 实例IP */
	private String instanceIp;

	/** 实例名 */
	private String instanceName;

	private int identity;

	private String druidName;

	private String dbType;

	private String driverClassName;

	private String url;

	private int waitThreadCount;

	private long notEmptyWaitCount;

	private long notEmptyWaitNanos;

	private int poolingCount;

	private int poolingPeak;

	private Date poolingPeakTime;

	private int activeCount;

	private int activePeak;

	private Date activePeakTime;

	private int initialSize;

	private int minIdle;

	private int maxActive;

	private int queryTimeout;

	private int transactionQueryTimeout;

	private int loginTimeout;

	private String validConnectionCheckerClassName;

	private String exceptionSorterClassName;

	private boolean testOnBorrow;

	private boolean testOnReturn;

	private boolean testWhileIdle;

	private boolean defaultAutoCommit;

	private boolean defaultReadOnly;

	private String defaultTransactionIsolation;

	private long logicConnectCount;

	private long logicCloseCount;

	private long logicConnectErrorCount;

	private long physicalConnectCount;

	private long physicalCloseCount;

	private long physicalConnectErrorCount;

	private long executeCount;

	private long errorCount;

	private long commitCount;

	private long rollbackCount;

	private long pSCacheAccessCount;

	private long pSCacheHitCount;

	private long pSCacheMissCount;

	private long startTransactionCount;

	private long[] transactionHistogram;

	private long[] connectionHoldTimeHistogram;

	private boolean removeAbandoned;

	private long clobOpenCount;

	private long blobOpenCount;

	private long keepAliveCheckCount;

	private boolean keepAlive;

	private boolean failFast;

	private long maxWait;

	private int maxWaitThreadCount;

	private boolean poolPreparedStatements;

	private int maxPoolPreparedStatementsPerConnectionSize;

	private long minEvictableIdleTimeMillis;

	private long maxEvictableIdleTimeMillis;

	private boolean logDifferentThread;

	private long recycleErrorCount;

	private long preparedStatementOpenCount;

	private long preparedStatementClosedCount;

	private boolean useUnfairLock;

	private boolean initGlobalvariants;

	private boolean initVariants;

	@Override
	public String toString() {
		return "DruidStatMessageDTO [msgId=" + msgId + ", domainId=" + domainId + ", eoappName=" + eoappName
				+ ", instanceIp=" + instanceIp + ", instanceName=" + instanceName + ", identity=" + identity
				+ ", druidName=" + druidName + ", dbType=" + dbType + ", driverClassName=" + driverClassName + ", url="
				+ url + ", waitThreadCount=" + waitThreadCount + ", notEmptyWaitCount=" + notEmptyWaitCount
				+ ", notEmptyWaitNanos=" + notEmptyWaitNanos + ", poolingCount=" + poolingCount + ", poolingPeak="
				+ poolingPeak + ", poolingPeakTime=" + poolingPeakTime + ", activeCount=" + activeCount
				+ ", activePeak=" + activePeak + ", activePeakTime=" + activePeakTime + ", initialSize=" + initialSize
				+ ", minIdle=" + minIdle + ", maxActive=" + maxActive + ", queryTimeout=" + queryTimeout
				+ ", transactionQueryTimeout=" + transactionQueryTimeout + ", loginTimeout=" + loginTimeout
				+ ", validConnectionCheckerClassName=" + validConnectionCheckerClassName + ", exceptionSorterClassName="
				+ exceptionSorterClassName + ", testOnBorrow=" + testOnBorrow + ", testOnReturn=" + testOnReturn
				+ ", testWhileIdle=" + testWhileIdle + ", defaultAutoCommit=" + defaultAutoCommit + ", defaultReadOnly="
				+ defaultReadOnly + ", defaultTransactionIsolation=" + defaultTransactionIsolation
				+ ", logicConnectCount=" + logicConnectCount + ", logicCloseCount=" + logicCloseCount
				+ ", logicConnectErrorCount=" + logicConnectErrorCount + ", physicalConnectCount="
				+ physicalConnectCount + ", physicalCloseCount=" + physicalCloseCount + ", physicalConnectErrorCount="
				+ physicalConnectErrorCount + ", executeCount=" + executeCount + ", errorCount=" + errorCount
				+ ", commitCount=" + commitCount + ", rollbackCount=" + rollbackCount + ", pSCacheAccessCount="
				+ pSCacheAccessCount + ", pSCacheHitCount=" + pSCacheHitCount + ", pSCacheMissCount=" + pSCacheMissCount
				+ ", startTransactionCount=" + startTransactionCount + ", transactionHistogram="
				+ Arrays.toString(transactionHistogram) + ", connectionHoldTimeHistogram="
				+ Arrays.toString(connectionHoldTimeHistogram) + ", removeAbandoned=" + removeAbandoned
				+ ", clobOpenCount=" + clobOpenCount + ", blobOpenCount=" + blobOpenCount + ", keepAliveCheckCount="
				+ keepAliveCheckCount + ", keepAlive=" + keepAlive + ", failFast=" + failFast + ", maxWait=" + maxWait
				+ ", maxWaitThreadCount=" + maxWaitThreadCount + ", poolPreparedStatements=" + poolPreparedStatements
				+ ", maxPoolPreparedStatementsPerConnectionSize=" + maxPoolPreparedStatementsPerConnectionSize
				+ ", minEvictableIdleTimeMillis=" + minEvictableIdleTimeMillis + ", maxEvictableIdleTimeMillis="
				+ maxEvictableIdleTimeMillis + ", logDifferentThread=" + logDifferentThread + ", recycleErrorCount="
				+ recycleErrorCount + ", preparedStatementOpenCount=" + preparedStatementOpenCount
				+ ", preparedStatementClosedCount=" + preparedStatementClosedCount + ", useUnfairLock=" + useUnfairLock
				+ ", initGlobalvariants=" + initGlobalvariants + ", initVariants=" + initVariants + "]";
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
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

	public int getIdentity() {
		return identity;
	}

	public void setIdentity(int identity) {
		this.identity = identity;
	}

	public String getDruidName() {
		return druidName;
	}

	public void setDruidName(String druidName) {
		this.druidName = druidName;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getWaitThreadCount() {
		return waitThreadCount;
	}

	public void setWaitThreadCount(int waitThreadCount) {
		this.waitThreadCount = waitThreadCount;
	}

	public long getNotEmptyWaitCount() {
		return notEmptyWaitCount;
	}

	public void setNotEmptyWaitCount(long notEmptyWaitCount) {
		this.notEmptyWaitCount = notEmptyWaitCount;
	}

	public long getNotEmptyWaitNanos() {
		return notEmptyWaitNanos;
	}

	public void setNotEmptyWaitNanos(long notEmptyWaitNanos) {
		this.notEmptyWaitNanos = notEmptyWaitNanos;
	}

	public int getPoolingCount() {
		return poolingCount;
	}

	public void setPoolingCount(int poolingCount) {
		this.poolingCount = poolingCount;
	}

	public int getPoolingPeak() {
		return poolingPeak;
	}

	public void setPoolingPeak(int poolingPeak) {
		this.poolingPeak = poolingPeak;
	}

	public Date getPoolingPeakTime() {
		return poolingPeakTime;
	}

	public void setPoolingPeakTime(Date poolingPeakTime) {
		this.poolingPeakTime = poolingPeakTime;
	}

	public int getActiveCount() {
		return activeCount;
	}

	public void setActiveCount(int activeCount) {
		this.activeCount = activeCount;
	}

	public int getActivePeak() {
		return activePeak;
	}

	public void setActivePeak(int activePeak) {
		this.activePeak = activePeak;
	}

	public Date getActivePeakTime() {
		return activePeakTime;
	}

	public void setActivePeakTime(Date activePeakTime) {
		this.activePeakTime = activePeakTime;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public int getTransactionQueryTimeout() {
		return transactionQueryTimeout;
	}

	public void setTransactionQueryTimeout(int transactionQueryTimeout) {
		this.transactionQueryTimeout = transactionQueryTimeout;
	}

	public int getLoginTimeout() {
		return loginTimeout;
	}

	public void setLoginTimeout(int loginTimeout) {
		this.loginTimeout = loginTimeout;
	}

	public String getValidConnectionCheckerClassName() {
		return validConnectionCheckerClassName;
	}

	public void setValidConnectionCheckerClassName(String validConnectionCheckerClassName) {
		this.validConnectionCheckerClassName = validConnectionCheckerClassName;
	}

	public String getExceptionSorterClassName() {
		return exceptionSorterClassName;
	}

	public void setExceptionSorterClassName(String exceptionSorterClassName) {
		this.exceptionSorterClassName = exceptionSorterClassName;
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public boolean isTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public boolean isDefaultAutoCommit() {
		return defaultAutoCommit;
	}

	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
	}

	public boolean isDefaultReadOnly() {
		return defaultReadOnly;
	}

	public void setDefaultReadOnly(boolean defaultReadOnly) {
		this.defaultReadOnly = defaultReadOnly;
	}

	public String getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	public void setDefaultTransactionIsolation(String defaultTransactionIsolation) {
		this.defaultTransactionIsolation = defaultTransactionIsolation;
	}

	public long getLogicConnectCount() {
		return logicConnectCount;
	}

	public void setLogicConnectCount(long logicConnectCount) {
		this.logicConnectCount = logicConnectCount;
	}

	public long getLogicCloseCount() {
		return logicCloseCount;
	}

	public void setLogicCloseCount(long logicCloseCount) {
		this.logicCloseCount = logicCloseCount;
	}

	public long getLogicConnectErrorCount() {
		return logicConnectErrorCount;
	}

	public void setLogicConnectErrorCount(long logicConnectErrorCount) {
		this.logicConnectErrorCount = logicConnectErrorCount;
	}

	public long getPhysicalConnectCount() {
		return physicalConnectCount;
	}

	public void setPhysicalConnectCount(long physicalConnectCount) {
		this.physicalConnectCount = physicalConnectCount;
	}

	public long getPhysicalCloseCount() {
		return physicalCloseCount;
	}

	public void setPhysicalCloseCount(long physicalCloseCount) {
		this.physicalCloseCount = physicalCloseCount;
	}

	public long getPhysicalConnectErrorCount() {
		return physicalConnectErrorCount;
	}

	public void setPhysicalConnectErrorCount(long physicalConnectErrorCount) {
		this.physicalConnectErrorCount = physicalConnectErrorCount;
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

	public long getCommitCount() {
		return commitCount;
	}

	public void setCommitCount(long commitCount) {
		this.commitCount = commitCount;
	}

	public long getRollbackCount() {
		return rollbackCount;
	}

	public void setRollbackCount(long rollbackCount) {
		this.rollbackCount = rollbackCount;
	}

	public long getpSCacheAccessCount() {
		return pSCacheAccessCount;
	}

	public void setpSCacheAccessCount(long pSCacheAccessCount) {
		this.pSCacheAccessCount = pSCacheAccessCount;
	}

	public long getpSCacheHitCount() {
		return pSCacheHitCount;
	}

	public void setpSCacheHitCount(long pSCacheHitCount) {
		this.pSCacheHitCount = pSCacheHitCount;
	}

	public long getpSCacheMissCount() {
		return pSCacheMissCount;
	}

	public void setpSCacheMissCount(long pSCacheMissCount) {
		this.pSCacheMissCount = pSCacheMissCount;
	}

	public long getStartTransactionCount() {
		return startTransactionCount;
	}

	public void setStartTransactionCount(long startTransactionCount) {
		this.startTransactionCount = startTransactionCount;
	}

	public long[] getTransactionHistogram() {
		return transactionHistogram;
	}

	public void setTransactionHistogram(long[] transactionHistogram) {
		this.transactionHistogram = transactionHistogram;
	}

	public long[] getConnectionHoldTimeHistogram() {
		return connectionHoldTimeHistogram;
	}

	public void setConnectionHoldTimeHistogram(long[] connectionHoldTimeHistogram) {
		this.connectionHoldTimeHistogram = connectionHoldTimeHistogram;
	}

	public boolean isRemoveAbandoned() {
		return removeAbandoned;
	}

	public void setRemoveAbandoned(boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
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

	public long getKeepAliveCheckCount() {
		return keepAliveCheckCount;
	}

	public void setKeepAliveCheckCount(long keepAliveCheckCount) {
		this.keepAliveCheckCount = keepAliveCheckCount;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isFailFast() {
		return failFast;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	public long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}

	public int getMaxWaitThreadCount() {
		return maxWaitThreadCount;
	}

	public void setMaxWaitThreadCount(int maxWaitThreadCount) {
		this.maxWaitThreadCount = maxWaitThreadCount;
	}

	public boolean getPoolPreparedStatements() {
		return poolPreparedStatements;
	}

	public void setPoolPreparedStatements(boolean poolPreparedStatements) {
		this.poolPreparedStatements = poolPreparedStatements;
	}

	public int getMaxPoolPreparedStatementsPerConnectionSize() {
		return maxPoolPreparedStatementsPerConnectionSize;
	}

	public void setMaxPoolPreparedStatementsPerConnectionSize(int maxPoolPreparedStatementsPerConnectionSize) {
		this.maxPoolPreparedStatementsPerConnectionSize = maxPoolPreparedStatementsPerConnectionSize;
	}

	public long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public long getMaxEvictableIdleTimeMillis() {
		return maxEvictableIdleTimeMillis;
	}

	public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
		this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
	}

	public boolean getLogDifferentThread() {
		return logDifferentThread;
	}

	public void setLogDifferentThread(boolean logDifferentThread) {
		this.logDifferentThread = logDifferentThread;
	}

	public long getRecycleErrorCount() {
		return recycleErrorCount;
	}

	public void setRecycleErrorCount(long recycleErrorCount) {
		this.recycleErrorCount = recycleErrorCount;
	}

	public long getPreparedStatementOpenCount() {
		return preparedStatementOpenCount;
	}

	public void setPreparedStatementOpenCount(long preparedStatementOpenCount) {
		this.preparedStatementOpenCount = preparedStatementOpenCount;
	}

	public long getPreparedStatementClosedCount() {
		return preparedStatementClosedCount;
	}

	public void setPreparedStatementClosedCount(long preparedStatementClosedCount) {
		this.preparedStatementClosedCount = preparedStatementClosedCount;
	}

	public boolean isUseUnfairLock() {
		return useUnfairLock;
	}

	public void setUseUnfairLock(boolean useUnfairLock) {
		this.useUnfairLock = useUnfairLock;
	}

	public boolean isInitGlobalvariants() {
		return initGlobalvariants;
	}

	public void setInitGlobalvariants(boolean initGlobalvariants) {
		this.initGlobalvariants = initGlobalvariants;
	}

	public boolean isInitVariants() {
		return initVariants;
	}

	public void setInitVariants(boolean initVariants) {
		this.initVariants = initVariants;
	}

	public String getEoappName() {
		return eoappName;
	}

	public void setEoappName(String eoappName) {
		this.eoappName = eoappName;
	}

}
