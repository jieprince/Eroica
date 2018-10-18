package com.sendtomoon.eroica.eoapp.context.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
//import org.unidal.cat.Cat;
//import org.unidal.cat.message.Transaction;

import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;
import com.sendtomoon.eroica.eoapp.EoAppException;
import com.sendtomoon.eroica.eoapp.sar.SARContext;
import com.sendtomoon.eroica.eoapp.sar.SARContextFactory;
import com.sendtomoon.eroica.eoapp.sar.event.SARStartupFailedEvent;

public class SARManagerBean implements SARManager, ApplicationContextAware {

	private Log logger = LogFactory.getLog(this.getClass());

	private final Lock lock = new ReentrantLock();

	private volatile String sarList;

	private volatile Map<String, SARContext> sars;

	private volatile SARContextFactory sarContextFactory;

	public SARManagerBean() {
		this.sars = new ConcurrentHashMap<String, SARContext>();
	}

	private volatile ConfigurableApplicationContext applicationContext = null;
	//

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	public SARContext getSARContext(String sarName) {
		return getSARContext(sarName, true);
	}

	public SARContext getSARContext(String sarName, boolean requiredExists) {
		SARContext sar = sars.get(sarName);
		if (sar == null && requiredExists) {
			throw new EoAppException("SAR<" + sarName + ">Not exists...");
		}
		return sar;
	}

	public void startupSARs(String sarList) {
		if (sarList == null)
			return;
		lock.lock();
		try {
			Set<String> sarNames = EroicaConfigUtils.split(sarList);
			_startupSARs(sarNames);
		} finally {
			lock.unlock();
		}

	}

	public void refreshSARs(String sarList) {
		if (sarList == null)
			return;
		lock.lock();
		try {
			Set<String> sarNames = EroicaConfigUtils.split(sarList);
			_refreshSARs(sarNames);
		} finally {
			lock.unlock();
		}
	}

	public boolean exists(String SARName) {
		return sars != null && sars.containsKey(SARName);
	}

	public void refreshSARs(Set<String> sarNewSet) {
		if (sarNewSet == null)
			return;
		lock.lock();
		try {
			_refreshSARs(sarNewSet);
		} finally {
			lock.unlock();
		}
	}

	protected void _refreshSARs(Set<String> newSARSet) {
		if (newSARSet == null) {
			newSARSet = new HashSet<String>(0);
		}
		Map<String, SARContext> curSARMap = this.sars;
		Set<String> curSARs = new HashSet<String>();
		curSARs.addAll(curSARMap.keySet());
		//
		if (logger.isInfoEnabled()) {
			logger.info("RefreshSARs, NewSARSet=" + newSARSet + ", and CurrentSARSet=" + curSARs);
		}
		for (String sarName : newSARSet) {
			if (!curSARMap.containsKey(sarName)) {
				startup(sarName);
			}
		}
		for (String sarName : curSARs) {
			if (!newSARSet.contains(sarName)) {
				SARContext removedSAR = curSARMap.remove((String) sarName);
				if (removedSAR != null) {
					removedSAR.shutdown();
				}
			}
		}
	}

	public void startupSARs(Set<String> sarNames) {
		lock.lock();
		try {
			_startupSARs(sarNames);
		} finally {
			lock.unlock();
		}
	}

	protected void _startupSARs(Set<String> sarNames) {
		if (sarNames == null || sarNames.size() == 0) {
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info("StartupSARs:" + sarNames);
		}
		for (String sarName : sarNames) {
			startup(sarName);
		}
	}

	@Override
	public List<SARContext> listSARContext() {
		List<SARContext> sarList = new ArrayList<SARContext>(sars.size());
		sarList.addAll(sars.values());
		if (sarList.size() > 0) {
			// 排序
			OrderComparator.sort(sarList);
		}
		return sarList;
	}

	public void shutdown() {
		if (sars == null)
			return;
//		Transaction t = Cat.newTransaction("SARManager.shutdown", "SARManager");
		lock.lock();
		try {
			Object[] locals = sars.keySet().toArray();
			for (Object key : locals) {
				try {
					sars.get(key).shutdown();
				} catch (Throwable th) {
					logger.error(th.getMessage(), th);
//					t.setStatus(th);
				}
			}
			sars.clear();

//			t.setSuccessStatus();
		} finally {
			lock.unlock();
//			t.complete();
		}
	}

	@Override
	public boolean shutdown(String sarName) {
		lock.lock();
		try {
			SARContext sar = sars.remove(sarName);
			if (sar != null) {
				sar.shutdown();
				return true;
			} else {
				logger.error("SAR:" + sarName + " Shutdown  failed,Cause not running . ");
				return false;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isRunning(String sarName) {
		SARContext sar = sars.get(sarName);
		return sar != null && sar.isRunning();
	}

	@Override
	public boolean startup(String sarName) {
		lock.lock();
		try {
			SARContext sar = sars.get(sarName);
			if (sar != null) {
				logger.error("Startup sar failed,Cause  sar<" + sarName + "> be running . ");
				return false;
			} else {
				SARContext SAR = _startupSAR(sarName);
				// 加入判断，组件正常启动的时候才把组件添加到心跳上下文
				if (SAR.isRunning()) {
					sars.put(sarName, SAR);
				}
				return SAR.isRunning();
			}
		} catch (Throwable th) {
			logger.error("Startup sar:" + sarName + " error,cause:" + th.getMessage(), th);
			return false;
		} finally {
			lock.unlock();
		}

	}

	protected SARContext _startupSAR(String sarName) {
		SARContext SAR = null;
//		Transaction t = null;
		try {
//			t = Cat.newTransaction("SARManager.startup", sarName);
			SAR = sarContextFactory.create(sarName);
//			t.setSuccessStatus();
		} catch (Throwable th) {
//			t.setStatus(th.getMessage());
//			Cat.logError(th);
		} finally {
//			t.complete();
		}

		if (SAR == null)
			return null;
		SAR.startup();
		if (!SAR.isRunning()) {
			SARStartupFailedEvent event = new SARStartupFailedEvent(SAR);
			// Pizza.getSpringContext().publishEvent(event);
			applicationContext.publishEvent(event);
		}
		return SAR;
	}

	public String getSarList() {
		return sarList;
	}

	public void setSarList(String sarList) {
		this.sarList = sarList;
	}

	public SARContextFactory getSarContextFactory() {
		return sarContextFactory;
	}

	public void setSarContextFactory(SARContextFactory sarContextFactory) {
		this.sarContextFactory = sarContextFactory;
	}

}
