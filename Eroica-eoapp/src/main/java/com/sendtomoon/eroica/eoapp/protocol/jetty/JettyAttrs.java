package com.sendtomoon.eroica.eoapp.protocol.jetty;

public class JettyAttrs {

	public static final int DEFAULT_JETTY_PORT = 8080;

	public static final String ATTR_KEY = "jettyAttrs";

	public static final String PROPERTIES_PREFIX = "eoapp.jetty.";

	private Integer port = DEFAULT_JETTY_PORT;

	private String webroot;

	private String contextPath = "/";

	private boolean websocketEnable;

	private int maxFormContentSize = -1;

	private String homepage;

	private int maxFormKeys = -1;

	private String webrootDirectory;

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getWebroot() {
		return webroot;
	}

	public void setWebroot(String webroot) {
		this.webroot = webroot;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public boolean isWebsocketEnable() {
		return websocketEnable;
	}

	public void setWebsocketEnable(boolean websocketEnable) {
		this.websocketEnable = websocketEnable;
	}

	public int getMaxFormContentSize() {
		return maxFormContentSize;
	}

	public void setMaxFormContentSize(int maxFormContentSize) {
		this.maxFormContentSize = maxFormContentSize;
	}

	public int getMaxFormKeys() {
		return maxFormKeys;
	}

	public void setMaxFormKeys(int maxFormKeys) {
		this.maxFormKeys = maxFormKeys;
	}

	public String getWebrootDirectory() {
		return webrootDirectory;
	}

	public void setWebrootDirectory(String webrootDirectory) {
		this.webrootDirectory = webrootDirectory;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

}
