package com.sendtomoon.eroica.fling.monitor;

public interface FlingMonitor {

	public static final String KEY_FLING_MONITOR_ENABLE = "fling.monitor.enable";

	boolean isEnable();

	String reportError(String msg, Throwable thrown, String sarName);

	String reportError(Throwable thrown, String sarName);

	String reportError(Throwable thrown);

	String reportError(String msg, Throwable thrown);

	String reportForSARStartupFailed(String sarName);

	String reportForSARStoped(String sarName);

	String reportForSARStartuped(String sarName);

}
