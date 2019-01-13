package com.sendtomoon.eroica.fling.monitor.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;

import com.alibaba.dubbo.config.ServiceConfig;
import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomoon.eroica.fling.monitor.FlingMonitorDubboServices;
import com.sendtomoon.eroica.fling.monitor.MonitorMsgRepository;
import com.sendtomoon.eroica.fling.monitor.dtos.MonitorMsgDTO;

public class FlingMonitorListener implements InitializingBean, DisposableBean, Ordered {

	private Log logger = LogFactory.getLog(this.getClass());

	private MonitorMsgRepository repository;

	private boolean exportService = true;

	private ServiceConfig<FlingMonitorDubboServices> _serviceConfig;

	public FlingMonitorListener() {

	}

	public FlingMonitorListener(MonitorMsgRepository repository) {
		this.repository = repository;
	}

	protected void handleMessage(MonitorMsgDTO msg) {
		try {
			MDCUtil.set();
			if (logger.isInfoEnabled()) {
				logger.info("Repository monitor message:" + msg);
			}
			repository.onMessage(msg);
		} catch (Exception e) {
			logger.error("Repository monitor message error for " + msg.getMsgId() + ",cause:" + e.getMessage(), e);
		} finally {
			MDCUtil.clear();
		}
	}

	@Override
	public void destroy() throws Exception {
		if (_serviceConfig != null) {
			try {
				_serviceConfig.unexport();
			} catch (Exception ex) {

			}
			_serviceConfig = null;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.exportService) {
			_serviceConfig = new ServiceConfig<FlingMonitorDubboServices>();
			_serviceConfig.setInterface(FlingMonitorDubboServices.class);
			_serviceConfig.setRef(new FlingMonitorDubboServices() {

				@Override
				public void sendMonitorMsg(MonitorMsgDTO msg) {
					handleMessage(msg);
				}

			});

			// --------------------------
			_serviceConfig.export();
		}
	}

	public MonitorMsgRepository getRepository() {
		return repository;
	}

	public void setRepository(MonitorMsgRepository repository) {
		this.repository = repository;
	}

	public ServiceConfig<FlingMonitorDubboServices> get_serviceConfig() {
		return _serviceConfig;
	}

	public void set_serviceConfig(ServiceConfig<FlingMonitorDubboServices> _serviceConfig) {
		this._serviceConfig = _serviceConfig;
	}

	public boolean isExportService() {
		return exportService;
	}

	public void setExportService(boolean exportService) {
		this.exportService = exportService;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
