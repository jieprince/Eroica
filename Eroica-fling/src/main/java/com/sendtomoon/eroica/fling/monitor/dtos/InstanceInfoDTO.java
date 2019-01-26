package com.sendtomoon.eroica.fling.monitor.dtos;

import java.util.Map;
import java.util.Properties;

import com.sendtomoon.eroica.common.utils.PNetUtils;

public class InstanceInfoDTO implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**主机名*/
	private String hostName;
	
	/**JDK版本*/
	private String  javaVersion;
	
	/**JDK供应商*/
	private String  javaVendor;
	
	/**java虚拟机名称*/
	private String javaVmName;
	
	/**java虚拟机版本*/
	private String javaVmVersion;
	
	
	/**操作系统的名称*/
	private String osName;
	
	/**操作系统的构架*/
	private String  osArch;
	
	/**操作系统的版本*/
	private String osVersion;
	
	/**当前登陆用户*/
	private String userName;
	
	/**最大可用CPU核数*/
	private int availableProcessors=-1;
	
	/**最大可用内存*/
	private int availableMemory=-1;
	
	/** PAPP版本 */
	private String pappVersion;
	
	
	
	public InstanceInfoDTO(){
		
	}
	
	public String toString(){
		StringBuilder str=new StringBuilder();
		str.append("{hostName=").append(hostName);
		str.append(",javaVersion=").append(javaVersion);
		str.append(",javaVendor=").append(javaVendor);
		str.append(",javaVmName=").append(javaVmName);
		str.append(",javaVmVersion=").append(this.javaVmVersion);
		str.append(",osName=").append(osName);
		str.append(",osArch=").append(osArch);
		str.append(",osVersion=").append(osVersion);
		str.append(",userName=").append(userName);
		str.append(",availableProcessors=").append(availableProcessors);
		str.append(",availableMemory=").append(availableMemory);
		str.append(",pappVersion=").append(pappVersion);
		str.append("}");
		return str.toString();
	}
	/***
	 *  
	 * @param systemProperties
	 */
	public InstanceInfoDTO(Properties systemProperties){
		Map<String, String> env = System.getenv();
		this.hostName=env.get("COMPUTERNAME");
		if(hostName==null){
			hostName=PNetUtils.getLocalAddress().getHostName();
		}
		//-----
		javaVersion=systemProperties.getProperty("java.version");
		this.javaVendor=systemProperties.getProperty("java.vendor");
		this.javaVmName=systemProperties.getProperty("java.vm.name");
		this.javaVmVersion=systemProperties.getProperty("java.vm.version");
		this.osName=systemProperties.getProperty("os.name");
		this.osVersion=systemProperties.getProperty("os.version");
		this.osArch=systemProperties.getProperty("os.arch");
		this.userName=System.getProperty("user.name");
	}
	
	/*public static void main(String args[]){
		java.util.Properties props=System.getProperties();
		 Map<String, String> map = System.getenv();
	        String userName = map.get("USERNAME");// 获取用户名
	        String computerName = map.get("COMPUTERNAME");// 获取计算机名
	        String userDomain = map.get("USERDOMAIN");// 获取计算机域名
	        System.out.println("用户名:    " + userName);
	        System.out.println("计算机名:    " + computerName);
	        System.out.println("计算机域名:    " + userDomain);
	        System.out.println("主机名：    " + PNetUtils.getLocalAddress().getHostName());
	        System.out.println("Java的运行环境版本：    " + props.getProperty("java.version"));
	        System.out.println("Java的运行环境供应商：    " + props.getProperty("java.vendor"));
	        System.out.println("Java供应商的URL：    " + props.getProperty("java.vendor.url"));
	        System.out.println("Java的安装路径：    " + props.getProperty("java.home"));
	        System.out.println("Java的虚拟机规范版本：    " + props.getProperty("java.vm.specification.version"));
	        System.out.println("Java的虚拟机规范供应商：    " + props.getProperty("java.vm.specification.vendor"));
	        System.out.println("Java的虚拟机规范名称：    " + props.getProperty("java.vm.specification.name"));
	        System.out.println("Java的虚拟机实现版本：    " + props.getProperty("java.vm.version"));
	        System.out.println("Java的虚拟机实现供应商：    " + props.getProperty("java.vm.vendor"));
	        System.out.println("Java的虚拟机实现名称：    " + props.getProperty("java.vm.name"));
	        System.out.println("Java运行时环境规范版本：    " + props.getProperty("java.specification.version"));
	        System.out.println("Java运行时环境规范供应商：    " + props.getProperty("java.specification.vender"));
	        System.out.println("Java运行时环境规范名称：    " + props.getProperty("java.specification.name"));
	        System.out.println("Java的类格式版本号：    " + props.getProperty("java.class.version"));
	        System.out.println("Java的类路径：    " + props.getProperty("java.class.path"));
	        System.out.println("加载库时搜索的路径列表：    " + props.getProperty("java.library.path"));
	        System.out.println("默认的临时文件路径：    " + props.getProperty("java.io.tmpdir"));
	        System.out.println("一个或多个扩展目录的路径：    " + props.getProperty("java.ext.dirs"));
	        System.out.println("操作系统的名称：    " + props.getProperty("os.name"));
	        System.out.println("操作系统的构架：    " + props.getProperty("os.arch"));
	        System.out.println("操作系统的版本：    " + props.getProperty("os.version"));
	        System.out.println("文件分隔符：    " + props.getProperty("file.separator"));
	        System.out.println("路径分隔符：    " + props.getProperty("path.separator"));
	        System.out.println("行分隔符：    " + props.getProperty("line.separator"));
	        System.out.println("用户的账户名称：    " + props.getProperty("user.name"));
	        System.out.println("用户的主目录：    " + props.getProperty("user.home"));
	        System.out.println("用户的当前工作目录：    " + props.getProperty("user.dir"));
	       
	        
	}*/

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}

	public String getJavaVmName() {
		return javaVmName;
	}

	public void setJavaVmName(String javaVmName) {
		this.javaVmName = javaVmName;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsArch() {
		return osArch;
	}

	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public int getAvailableMemory() {
		return availableMemory;
	}

	public void setAvailableMemory(int availableMemory) {
		this.availableMemory = availableMemory;
	}

	public String getJavaVmVersion() {
		return javaVmVersion;
	}

	public void setJavaVmVersion(String javaVmVersion) {
		this.javaVmVersion = javaVmVersion;
	}

	public String getPappVersion() {
		return pappVersion;
	}

	public void setPappVersion(String pappVersion) {
		this.pappVersion = pappVersion;
	}

	
	
	
}
