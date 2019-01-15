package com.sendtomoon.eroica.datasource.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.alibaba.druid.stat.DruidStatManagerFacade;

public class DruidSqlMonitorBean implements InitializingBean, DisposableBean {
	
	private static Logger logger = LoggerFactory.getLogger(DruidSqlMonitorBean.class);

	private DruidSqlMessagePublisher publisher;

	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	public DruidSqlMonitorBean() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("Start druid sql monitor.");

		publisher = new DruidSqlMessagePublisher();

		Thread monitorThread = new Thread() {
			@Override
			public void run() {
				report();
			}
		};
		scheduledThreadPool.scheduleAtFixedRate(monitorThread, 1L, 10L, TimeUnit.SECONDS);
	}

	@Override
	public void destroy() throws Exception {
		logger.info("Stop druid sql monitor.");

		if (publisher != null) {
			publisher.shutdown();
			publisher = null;
		}

		scheduledThreadPool.shutdown();
	}

	protected void report() {
		Set<Object> dataSources = DruidDataSourceStatManager.getInstances().keySet();
		for(Object dataSource : dataSources) {
			int identity = System.identityHashCode(dataSource);
			List<Map<String, Object>> statDataList = DruidStatManagerFacade.getInstance().getSqlStatDataList(identity);
			for (Map<String, Object> statData : statDataList) {
				try {
					DruidSqlMessageDTO msg = map2Msg(statData);
					msg.setMsgId(MessageUtil.generateMsgId());
					msg.setIdentity(identity);
					
					publisher.pushSqlMsg(msg);
				} catch(Exception e) {
					logger.error("Error sending druid sql msg : ", e);
				}		
			}
		}
	}
	
	private DruidSqlMessageDTO map2Msg(Map<String, Object> statData) {
		DruidSqlMessageDTO msg = new DruidSqlMessageDTO();
		msg.setSqlId(MessageUtil.getLongValue(statData.get("ID")));
		msg.setDruidSql(MessageUtil.getStringValue(statData.get("SQL")));
		msg.setExecuteCount(MessageUtil.getLongValue(statData.get("ExecuteCount")));
		msg.setErrorCount(MessageUtil.getLongValue(statData.get("ErrorCount")));
		msg.setTotalTime(MessageUtil.getLongValue(statData.get("TotalTime")));
		msg.setLastTime(MessageUtil.getDateValue(statData.get("LastTime")));
		msg.setMaxTimespan(MessageUtil.getLongValue(statData.get("MaxTimespan")));
		msg.setLastError(MessageUtil.getStringValue(statData.get("LastError")));
		msg.setEffectedRowCount(MessageUtil.getLongValue(statData.get("EffectedRowCount")));
		msg.setFetchRowCount(MessageUtil.getLongValue(statData.get("FetchRowCount")));
		msg.setMaxTimespanOccurTime(MessageUtil.getDateValue(statData.get("MaxTimespanOccurTime")));
		msg.setBatchSizeMax(MessageUtil.getLongValue(statData.get("BatchSizeMax")));
		msg.setBatchSizeTotal(MessageUtil.getLongValue(statData.get("BatchSizeTotal")));
		msg.setConcurrentMax(MessageUtil.getLongValue(statData.get("ConcurrentMax")));
		msg.setRunningCount(MessageUtil.getLongValue(statData.get("RunningCount")));
		msg.setDruidName(MessageUtil.getStringValue(statData.get("Name")));
		msg.setDruidFile(MessageUtil.getStringValue(statData.get("File")));
		msg.setLastErrorMessage(MessageUtil.getStringValue(statData.get("LastErrorMessage")));
		msg.setLastErrorClass(MessageUtil.getStringValue(statData.get("LastErrorClass")));
		msg.setLastErrorStackTrace(MessageUtil.getStringValue(statData.get("LastErrorStackTrace")));
		msg.setLastErrorTime(MessageUtil.getDateValue(statData.get("LastErrorTime")));
		msg.setDbType(MessageUtil.getStringValue(statData.get("DbType")));
		msg.setInTransactionCount(MessageUtil.getLongValue(statData.get("InTransactionCount")));
		msg.setHistogram(MessageUtil.getLongArrayValue(statData.get("Histogram")));
		msg.setLastSlowParameters(MessageUtil.getStringValue(statData.get("LastSlowParameters")));
		msg.setResultSetHoldTime(MessageUtil.getLongValue(statData.get("ResultSetHoldTime")));
		msg.setExecuteAndResultSetHoldTime(MessageUtil.getLongValue(statData.get("ExecuteAndResultSetHoldTime")));
		msg.setFetchRowCountHistogram(MessageUtil.getLongArrayValue(statData.get("FetchRowCountHistogram")));
		msg.setEffectedRowCountHistogram(MessageUtil.getLongArrayValue(statData.get("EffectedRowCountHistogram")));		
		msg.setExecuteAndResultHoldTimeHistogram(MessageUtil.getLongArrayValue(statData.get("ExecuteAndResultHoldTimeHistogram")));
		msg.setEffectedRowCountMax(MessageUtil.getLongValue(statData.get("EffectedRowCountMax")));
		msg.setFetchRowCountMax(MessageUtil.getLongValue(statData.get("FetchRowCountMax")));
		msg.setClobOpenCount(MessageUtil.getLongValue(statData.get("ClobOpenCount")));
		msg.setBlobOpenCount(MessageUtil.getLongValue(statData.get("BlobOpenCount")));
		msg.setReadStringLength(MessageUtil.getLongValue(statData.get("ReadStringLength")));
		msg.setReadBytesLength(MessageUtil.getLongValue(statData.get("ReadBytesLength")));
		msg.setInputStreamOpenCount(MessageUtil.getLongValue(statData.get("InputStreamOpenCount")));
		msg.setReaderOpenCount(MessageUtil.getLongValue(statData.get("ReaderOpenCount")));
		msg.setSqlHash(MessageUtil.getLongValue(statData.get("HASH")));
		return msg;
	}

}
