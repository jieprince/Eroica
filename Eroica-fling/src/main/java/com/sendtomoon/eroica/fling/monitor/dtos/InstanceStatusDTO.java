package com.sendtomoon.eroica.fling.monitor.dtos;

public class InstanceStatusDTO implements java.io.Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**进程(JVM)可使用(已分配)内存,单位:MB. */   
    private int totalMemory=-1;   
     
    /**进程(JVM)剩余内存,单位:MB. */   
    private int freeMemory=-1;   
     
    /**进程(JVM)最大可使用内存,单位:MB. */   
    private int maxMemory=-1;   
    
    /**进程(JVM)已用内存,单位:MB*/
    private int usedMemory=-1;
     
   
    
    /**
	 * 进程内CPU平均负载，非准确的参考值，单位：百分比，如：64即%64*/
	private double jvmCPURatioAverage=-1d;
	
	/**
	 * 主机CPU负载，单位：百分比，如：64即%64*/
	private double hostCPURatio=-1;
	
	/**进程(JVM)内开启线程数*/
	private int threadTotal=-1;
	
	/**进程ID*/
	private String pid;
	
	
	/**可用CPU核数*/
	private int availableProcessors=-1;

	public String toString(){
		StringBuilder str=new StringBuilder();
		str.append("{totalMemory=").append(totalMemory);
		str.append(",freeMemory=").append(freeMemory);
		str.append(",maxMemory=").append(maxMemory);
		str.append(",usedMemory=").append(usedMemory);
		str.append(",jvmCPURatioAverage=").append(jvmCPURatioAverage);
		str.append(",hostCPURatio=").append(hostCPURatio);
		str.append(",threadTotal=").append(threadTotal);
		str.append(",availableProcessors=").append(availableProcessors);
		str.append(",pid=").append(pid);
		str.append("}");
		return str.toString();
	}

	public int getTotalMemory() {
		return totalMemory;
	}


	public void setTotalMemory(int totalMemory) {
		this.totalMemory = totalMemory;
	}


	public int getFreeMemory() {
		return freeMemory;
	}


	public void setFreeMemory(int freeMemory) {
		this.freeMemory = freeMemory;
	}


	public int getMaxMemory() {
		return maxMemory;
	}


	public void setMaxMemory(int maxMemory) {
		this.maxMemory = maxMemory;
	}


	public int getUsedMemory() {
		return usedMemory;
	}


	public void setUsedMemory(int usedMemory) {
		this.usedMemory = usedMemory;
	}


	public double getJvmCPURatioAverage() {
		return jvmCPURatioAverage;
	}


	public void setJvmCPURatioAverage(double jvmCPURatioAverage) {
		this.jvmCPURatioAverage = jvmCPURatioAverage;
	}


	public double getHostCPURatio() {
		return hostCPURatio;
	}


	public void setHostCPURatio(double hostCPURatio) {
		this.hostCPURatio = hostCPURatio;
	}


	public int getThreadTotal() {
		return threadTotal;
	}


	public void setThreadTotal(int threadTotal) {
		this.threadTotal = threadTotal;
	}


	public int getAvailableProcessors() {
		return availableProcessors;
	}


	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}


	

	
	

	
}
