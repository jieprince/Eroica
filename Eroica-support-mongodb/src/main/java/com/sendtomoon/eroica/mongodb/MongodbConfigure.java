package com.sendtomoon.eroica.mongodb;

public class MongodbConfigure {

	private String servers;

	private String dbname;

	private String user;

	private String password;

	private String passwordKey;

	private String passwordProvider;

	private Boolean alwaysUseMBeans;

	private Boolean cursorFinalizerEnabled;

	private Boolean autoConnectRetry;

	private Long maxAutoConnectRetryTime;

	private Boolean socketKeepAlive;

	private Integer maxWaitTime;

	// 主机连接数
	private Integer connectionsPerHost;

	// 线程队列数
	private Integer threadsAllowedToBlockForConnectionMultiplier;

	// socket超时时间
	private Integer socketTimeout;

	private Integer connectTimeout;

	// 从节点是否支持读操作
	private Boolean slaveReadbale;

	private Integer acceptableLatencyDifference;

	private Integer maxConnectionIdleTime;

	private Integer maxConnectionLifeTime;

	private Integer minConnectionsPerHost;

	private String requiredReplicaSetName;

	private Integer heartbeatConnectRetryFrequency;

	private Integer heartbeatConnectTimeout;

	private Integer heartbeatFrequency;

	private Integer heartbeatSocketTimeout;

	private Integer heartbeatThreadCount;

	private String credentialsType;

	private String authorityType;

	private String writeConcern;

	public MongodbConfigure() {
		this.setConnectionsPerHost(10);
		this.setSocketTimeout(2000);
		this.setThreadsAllowedToBlockForConnectionMultiplier(10);
	}

	public MongodbConfigure(String servers, String dbname) {
		this.servers = servers;
		this.dbname = dbname;
	}

	public MongodbConfigure(String servers, String dbname, String user) {
		this.servers = servers;
		this.dbname = dbname;
		this.user = user;
	}

	public MongodbConfigure(String servers, String dbname, String user, String password) {
		this.servers = servers;
		this.dbname = dbname;
		this.user = user;
		this.password = password;
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getAlwaysUseMBeans() {
		return alwaysUseMBeans;
	}

	public void setAlwaysUseMBeans(Boolean alwaysUseMBeans) {
		this.alwaysUseMBeans = alwaysUseMBeans;
	}

	public Boolean getCursorFinalizerEnabled() {
		return cursorFinalizerEnabled;
	}

	public void setCursorFinalizerEnabled(Boolean cursorFinalizerEnabled) {
		this.cursorFinalizerEnabled = cursorFinalizerEnabled;
	}

	public Boolean getSocketKeepAlive() {
		return socketKeepAlive;
	}

	public void setSocketKeepAlive(Boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
	}

	public Integer getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public Integer getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	public void setThreadsAllowedToBlockForConnectionMultiplier(Integer threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}

	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Boolean getSlaveReadbale() {
		return slaveReadbale;
	}

	public void setSlaveReadbale(Boolean slaveReadbale) {
		this.slaveReadbale = slaveReadbale;
	}

	public Integer getAcceptableLatencyDifference() {
		return acceptableLatencyDifference;
	}

	public void setAcceptableLatencyDifference(Integer acceptableLatencyDifference) {
		this.acceptableLatencyDifference = acceptableLatencyDifference;
	}

	public Integer getMaxConnectionIdleTime() {
		return maxConnectionIdleTime;
	}

	public void setMaxConnectionIdleTime(Integer maxConnectionIdleTime) {
		this.maxConnectionIdleTime = maxConnectionIdleTime;
	}

	public Integer getMaxConnectionLifeTime() {
		return maxConnectionLifeTime;
	}

	public void setMaxConnectionLifeTime(Integer maxConnectionLifeTime) {
		this.maxConnectionLifeTime = maxConnectionLifeTime;
	}

	public Integer getMinConnectionsPerHost() {
		return minConnectionsPerHost;
	}

	public void setMinConnectionsPerHost(Integer minConnectionsPerHost) {
		this.minConnectionsPerHost = minConnectionsPerHost;
	}

	public String getRequiredReplicaSetName() {
		return requiredReplicaSetName;
	}

	public void setRequiredReplicaSetName(String requiredReplicaSetName) {
		this.requiredReplicaSetName = requiredReplicaSetName;
	}

	public Integer getHeartbeatConnectRetryFrequency() {
		return heartbeatConnectRetryFrequency;
	}

	public void setHeartbeatConnectRetryFrequency(Integer heartbeatConnectRetryFrequency) {
		this.heartbeatConnectRetryFrequency = heartbeatConnectRetryFrequency;
	}

	public Integer getHeartbeatConnectTimeout() {
		return heartbeatConnectTimeout;
	}

	public void setHeartbeatConnectTimeout(Integer heartbeatConnectTimeout) {
		this.heartbeatConnectTimeout = heartbeatConnectTimeout;
	}

	public Integer getHeartbeatFrequency() {
		return heartbeatFrequency;
	}

	public void setHeartbeatFrequency(Integer heartbeatFrequency) {
		this.heartbeatFrequency = heartbeatFrequency;
	}

	public Integer getHeartbeatSocketTimeout() {
		return heartbeatSocketTimeout;
	}

	public void setHeartbeatSocketTimeout(Integer heartbeatSocketTimeout) {
		this.heartbeatSocketTimeout = heartbeatSocketTimeout;
	}

	public Integer getHeartbeatThreadCount() {
		return heartbeatThreadCount;
	}

	public void setHeartbeatThreadCount(Integer heartbeatThreadCount) {
		this.heartbeatThreadCount = heartbeatThreadCount;
	}

	public String getCredentialsType() {
		return credentialsType;
	}

	public void setCredentialsType(String credentialsType) {
		this.credentialsType = credentialsType;
	}

	public String getPasswordKey() {
		return passwordKey;
	}

	public void setPasswordKey(String passwordKey) {
		this.passwordKey = passwordKey;
	}

	public String getWriteConcern() {
		return writeConcern;
	}

	public void setWriteConcern(String writeConcern) {
		this.writeConcern = writeConcern;
	}

	public String getAuthorityType() {
		return authorityType;
	}

	public void setAuthorityType(String authorityType) {
		this.authorityType = authorityType;
	}

	public Boolean getAutoConnectRetry() {
		return autoConnectRetry;
	}

	public void setAutoConnectRetry(Boolean autoConnectRetry) {
		this.autoConnectRetry = autoConnectRetry;
	}

	public Long getMaxAutoConnectRetryTime() {
		return maxAutoConnectRetryTime;
	}

	public void setMaxAutoConnectRetryTime(Long maxAutoConnectRetryTime) {
		this.maxAutoConnectRetryTime = maxAutoConnectRetryTime;
	}

	public String getPasswordProvider() {
		return passwordProvider;
	}

	public void setPasswordProvider(String passwordProvider) {
		this.passwordProvider = passwordProvider;
	}

}
