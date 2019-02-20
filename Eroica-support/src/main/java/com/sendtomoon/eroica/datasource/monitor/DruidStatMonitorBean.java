package com.sendtomoon.eroica.datasource.monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.sendtomoon.eroica.common.utils.InstanceSystemPropertyUtils;
import com.sendtomoon.eroica.common.utils.PNetUtils;
import com.sendtomoon.eroica.pizza.PizzaConstants;

/**
 * Druid数据库连接池监控收集器
 */
public class DruidStatMonitorBean implements InitializingBean, DisposableBean {

	private static Logger logger = LoggerFactory.getLogger(DruidStatMonitorBean.class);

	private DruidStatMessagePublisher publisher;

	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	public DruidStatMonitorBean() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("Start druid stat monitor.");

		publisher = new DruidStatMessagePublisher();

		Thread monitorThread = new Thread() {
			@Override
			public void run() {
				report();
			}
		};
		scheduledThreadPool.scheduleAtFixedRate(monitorThread, 1L, 5L, TimeUnit.SECONDS);
	}

	@Override
	public void destroy() throws Exception {
		logger.info("Stop druid stat monitor.");

		if (publisher != null) {
			publisher.shutdown();
			publisher = null;
		}

		scheduledThreadPool.shutdown();
	}

	protected void report() {
		List<Map<String, Object>> statDataList = DruidStatManagerFacade.getInstance().getDataSourceStatDataList();
		for (Map<String, Object> statData : statDataList) {
			try {
				DruidStatMessageDTO msg = map2Msg(statData);
				msg.setMsgId(MessageUtil.generateMsgId());
				msg.setDomainId(PizzaConstants.getProperty(PizzaConstants.KEY_DOMAIN_ID));
				msg.setEoappName(PizzaConstants.getAppName());
				msg.setInstanceIp(PNetUtils.getLocalHost());
				msg.setInstanceName(InstanceSystemPropertyUtils.getInstanceName());

				publisher.pushMonitorMsg(msg);
			} catch (Exception e) {
				logger.error("Error sending druid stat msg : ", e);
			}
		}
	}

	private DruidStatMessageDTO map2Msg(Map<String, Object> statData) {
		DruidStatMessageDTO msg = new DruidStatMessageDTO();
		msg.setIdentity(MessageUtil.getIntValue(statData.get("Identity")));
		msg.setDruidName(MessageUtil.getStringValue(statData.get("Name")));
		msg.setDbType(MessageUtil.getStringValue(statData.get("DbType")));
		msg.setDriverClassName(MessageUtil.getStringValue(statData.get("DriverClassName")));
		msg.setUrl(MessageUtil.getStringValue(statData.get("URL")));
		msg.setWaitThreadCount(MessageUtil.getIntValue(statData.get("WaitThreadCount")));
		msg.setNotEmptyWaitCount(MessageUtil.getLongValue(statData.get("NotEmptyWaitCount")));
		msg.setNotEmptyWaitNanos(MessageUtil.getLongValue(statData.get("NotEmptyWaitNanos")));
		msg.setPoolingCount(MessageUtil.getIntValue(statData.get("PoolingCount")));
		msg.setPoolingPeak(MessageUtil.getIntValue(statData.get("PoolingPeak")));
		msg.setPoolingPeakTime(MessageUtil.getDateValue(statData.get("PoolingPeakTime")));
		msg.setActiveCount(MessageUtil.getIntValue(statData.get("ActiveCount")));
		msg.setActivePeak(MessageUtil.getIntValue(statData.get("ActivePeak")));
		msg.setActivePeakTime(MessageUtil.getDateValue(statData.get("ActivePeakTime")));
		msg.setInitialSize(MessageUtil.getIntValue(statData.get("InitialSize")));
		msg.setMinIdle(MessageUtil.getIntValue(statData.get("MinIdle")));
		msg.setMaxActive(MessageUtil.getIntValue(statData.get("MaxActive")));
		msg.setQueryTimeout(MessageUtil.getIntValue(statData.get("QueryTimeout")));
		msg.setTransactionQueryTimeout(MessageUtil.getIntValue(statData.get("TransactionQueryTimeout")));
		msg.setLoginTimeout(MessageUtil.getIntValue(statData.get("LoginTimeout")));
		msg.setValidConnectionCheckerClassName(
				MessageUtil.getStringValue(statData.get("ValidConnectionCheckerClassName")));
		msg.setExceptionSorterClassName(MessageUtil.getStringValue(statData.get("ExceptionSorterClassName")));
		msg.setTestOnBorrow(MessageUtil.getBooleanValue(statData.get("TestOnBorrow")));
		msg.setTestOnReturn(MessageUtil.getBooleanValue(statData.get("TestOnReturn")));
		msg.setTestWhileIdle(MessageUtil.getBooleanValue(statData.get("TestWhileIdle")));
		msg.setDefaultAutoCommit(MessageUtil.getBooleanValue(statData.get("DefaultAutoCommit")));
		msg.setDefaultReadOnly(MessageUtil.getBooleanValue(statData.get("DefaultReadOnly")));
		msg.setDefaultTransactionIsolation(MessageUtil.getStringValue(statData.get("DefaultTransactionIsolation")));
		msg.setLogicConnectCount(MessageUtil.getLongValue(statData.get("LogicConnectCount")));
		msg.setLogicCloseCount(MessageUtil.getLongValue(statData.get("LogicCloseCount")));
		msg.setLogicConnectErrorCount(MessageUtil.getLongValue(statData.get("LogicConnectErrorCount")));
		msg.setPhysicalConnectCount(MessageUtil.getLongValue(statData.get("PhysicalConnectCount")));
		msg.setPhysicalCloseCount(MessageUtil.getLongValue(statData.get("PhysicalCloseCount")));
		msg.setPhysicalConnectErrorCount(MessageUtil.getLongValue(statData.get("PhysicalConnectErrorCount")));
		msg.setExecuteCount(MessageUtil.getLongValue(statData.get("ExecuteCount")));
		msg.setErrorCount(MessageUtil.getLongValue(statData.get("ErrorCount")));
		msg.setCommitCount(MessageUtil.getLongValue(statData.get("CommitCount")));
		msg.setRollbackCount(MessageUtil.getLongValue(statData.get("RollbackCount")));
		msg.setpSCacheAccessCount(MessageUtil.getLongValue(statData.get("PSCacheAccessCount")));
		msg.setpSCacheHitCount(MessageUtil.getLongValue(statData.get("PSCacheHitCount")));
		msg.setpSCacheMissCount(MessageUtil.getLongValue(statData.get("PSCacheMissCount")));
		msg.setStartTransactionCount(MessageUtil.getLongValue(statData.get("StartTransactionCount")));
		msg.setTransactionHistogram(MessageUtil.getLongArrayValue(statData.get("TransactionHistogram")));
		msg.setConnectionHoldTimeHistogram(MessageUtil.getLongArrayValue(statData.get("ConnectionHoldTimeHistogram")));
		msg.setRemoveAbandoned(MessageUtil.getBooleanValue(statData.get("RemoveAbandoned")));
		msg.setClobOpenCount(MessageUtil.getLongValue(statData.get("ClobOpenCount")));
		msg.setBlobOpenCount(MessageUtil.getLongValue(statData.get("BlobOpenCount")));
		msg.setKeepAliveCheckCount(MessageUtil.getLongValue(statData.get("KeepAliveCheckCount")));
		msg.setKeepAlive(MessageUtil.getBooleanValue(statData.get("KeepAlive")));
		msg.setFailFast(MessageUtil.getBooleanValue(statData.get("FailFast")));
		msg.setMaxWait(MessageUtil.getLongValue(statData.get("MaxWait")));// 类型错误
		msg.setMaxWaitThreadCount(MessageUtil.getIntValue(statData.get("MaxWaitThreadCount")));
		msg.setPoolPreparedStatements(MessageUtil.getBooleanValue(statData.get("PoolPreparedStatements")));// 类型错误
		msg.setMaxPoolPreparedStatementsPerConnectionSize(
				MessageUtil.getIntValue(statData.get("MaxPoolPreparedStatementsPerConnectionSize")));
		msg.setMinEvictableIdleTimeMillis(MessageUtil.getLongValue(statData.get("MinEvictableIdleTimeMillis")));// 类型错误
		msg.setMaxEvictableIdleTimeMillis(MessageUtil.getLongValue(statData.get("MaxEvictableIdleTimeMillis")));// 类型错误
		msg.setLogDifferentThread(MessageUtil.getBooleanValue(statData.get("LogDifferentThread")));// 类型错误
		msg.setRecycleErrorCount(MessageUtil.getLongValue(statData.get("RecycleErrorCount")));// 类型错误
		msg.setPreparedStatementOpenCount(MessageUtil.getLongValue(statData.get("PreparedStatementOpenCount")));// 类型错误
		msg.setPreparedStatementClosedCount(MessageUtil.getLongValue(statData.get("PreparedStatementClosedCount")));// 类型错误
		msg.setUseUnfairLock(MessageUtil.getBooleanValue(statData.get("UseUnfairLock")));
		msg.setInitGlobalvariants(MessageUtil.getBooleanValue(statData.get("InitGlobalvariants")));
		msg.setInitVariants(MessageUtil.getBooleanValue(statData.get("InitVariants")));
		return msg;
	}

}
