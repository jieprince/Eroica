package com.sendtomoon.eroica.fling.monitor.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sendtomoon.eroica.fling.monitor.dtos.InstanceStatusDTO;

public class MonitorCollectServiceImpl implements IMonitorCollectService {

	private Log logger = LogFactory.getLog(this.getClass());

	private static String linuxVersion = null;

	private static int mb = 1024 * 1024;

	public int getThreadTotal() {
		// 获得线程总数
		ThreadGroup parentThread;
		for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
				.getParent() != null; parentThread = parentThread.getParent())
			;
		int totalThread = parentThread.activeCount();
		return totalThread;
	}

	public void peekCPUInfos(InstanceStatusDTO status) {
		status.setHostCPURatio(this.getHostCPURatio());
		status.setThreadTotal(this.getThreadTotal());
		//
		OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		status.setJvmCPURatioAverage(osmxb.getSystemLoadAverage());
		status.setAvailableProcessors(osmxb.getAvailableProcessors());
		//
		String pinfo = ManagementFactory.getRuntimeMXBean().getName();
		if (pinfo != null && pinfo.length() > 0) {
			String pid = pinfo.split("@")[0];
			status.setPid(pid);
		}
	}

	@Override
	public int getAvailableProcessors() {
		OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		return osmxb.getAvailableProcessors();
	}

	@Override
	public int getAvailableMemory() {
		return (int) (Runtime.getRuntime().maxMemory() / mb);
	}

	@Override
	public InstanceStatusDTO getInstanceStatus() {
		InstanceStatusDTO status = new InstanceStatusDTO();
		peekMemoryInfo(status);
		peekCPUInfos(status);
		return status;
	}

	public void peekMemoryInfo(InstanceStatusDTO memoryInfo) {
		// 可使用内存
		int totalMemory = (int) (Runtime.getRuntime().totalMemory() / mb);
		// 剩余内存
		int freeMemory = (int) (Runtime.getRuntime().freeMemory() / mb);
		// 最大可使用内存
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / mb);

		// 构造返回对象
		memoryInfo.setFreeMemory(freeMemory);

		memoryInfo.setMaxMemory(maxMemory);
		//
		memoryInfo.setTotalMemory(totalMemory);
		memoryInfo.setUsedMemory(totalMemory - freeMemory);
	}

	public double getHostCPURatio() {
		// 操作系统
		String osName = System.getProperty("os.name");
		double cpuRatio = 0;
		if (osName.toLowerCase().startsWith("windows")) {
			cpuRatio = -1;
		} else {
			linuxVersion = System.getProperty("os.version");
			cpuRatio = this.getCpuRateForLinux();
		}
		return cpuRatio;
	}

	private double getCpuRateForLinux() {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader brStat = null;
		StringTokenizer tokenStat = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Get usage rate of CUP , linux version: " + linuxVersion);
			}
			Process process = Runtime.getRuntime().exec("top -b -n 1");
			is = process.getInputStream();
			isr = new InputStreamReader(is);
			brStat = new BufferedReader(isr);
			brStat.readLine();
			brStat.readLine();
			String infoLine = brStat.readLine();
			tokenStat = new StringTokenizer(infoLine);
			tokenStat.nextToken();
			tokenStat.nextToken();
			tokenStat.nextToken();
			tokenStat.nextToken();
			String cpuUsage = tokenStat.nextToken();
			logger.info("CUP usage rate info  : " + infoLine);
			if (cpuUsage.endsWith("%id,")) {
				double ratio = Double.parseDouble(cpuUsage.substring(0, cpuUsage.indexOf("%")));
				return Math.round((100d - ratio) * 100) / 100d;
			}
			return -1d;
		} catch (IOException ioe) {
			logger.info(ioe.getMessage(), ioe);
			return -1d;
		} finally {
			freeResource(is, isr, brStat);
		}

	}

	private void freeResource(InputStream is, InputStreamReader isr, BufferedReader br) {
		try {
			if (is != null)
				is.close();
			if (isr != null)
				isr.close();
			if (br != null)
				br.close();
		} catch (IOException ioe) {
			logger.info(ioe.getMessage());
		}
	}

	/**
	 * 测试方法.
	 * 
	 * @param args
	 * @throws Exception
	 * @author GuoHuang
	 */
	public static void main(String[] args) throws Exception {
		IMonitorCollectService service = new MonitorCollectServiceImpl();
		InstanceStatusDTO statusInfo = service.getInstanceStatus();
		//
		System.out.println("进程内线程数=" + statusInfo.getThreadTotal());
		System.out.println("CPU使用率=" + statusInfo.getHostCPURatio());
		System.out.println("参考平均CPU负载=" + statusInfo.getJvmCPURatioAverage());
		System.out.println("可使用CPU核数=" + statusInfo.getAvailableProcessors());
		//
		System.out.println("可使用内存=" + statusInfo.getTotalMemory());
		System.out.println("剩余内存=" + statusInfo.getFreeMemory());
		System.out.println("最大可使用内存=" + statusInfo.getMaxMemory());
		System.out.println("最大可使用内存=" + ManagementFactory.getRuntimeMXBean().getName());
	}

}
