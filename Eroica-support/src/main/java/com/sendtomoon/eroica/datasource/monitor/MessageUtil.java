package com.sendtomoon.eroica.datasource.monitor;

import java.util.Date;
import java.util.UUID;

public final class MessageUtil {
	
	public static String generateMsgId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	public static String getStringValue(Object obj) {
		if(obj == null) {
			return null;
		} else {
			return (String)obj;
		}
	}
	
	public static int getIntValue(Object obj) {
		if(obj == null) {
			return 0;
		} else {
			return (Integer)obj;
		}
	}
	
	public static long getLongValue(Object obj) {
		if(obj == null) {
			return 0L;
		} else {
			return (Long)obj;
		}
	}
	
	public static boolean getBooleanValue(Object obj) {
		if(obj == null) {
			return false;
		} else {
			return (Boolean)obj;
		}
	}
	
	public static Date getDateValue(Object obj) {
		if(obj == null) {
			return null;
		} else {
			return (Date)obj;
		}
	}
	
	public static long[] getLongArrayValue(Object obj) {
		if(obj == null) {
			return null;
		} else {
			return (long[])obj;
		}
	}

}
