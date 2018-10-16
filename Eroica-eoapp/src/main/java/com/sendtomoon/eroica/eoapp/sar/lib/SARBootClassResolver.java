package com.sendtomoon.eroica.eoapp.sar.lib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sendtomoon.eroica.eoapp.sar.SARAttrs;


public class SARBootClassResolver {
	
	
	private static final String BOOT_CLASS_PROPERTY_NAME="sar.boot.class";
	
	
	protected Log logger=LogFactory.getLog(this.getClass());

	
	public Class<?> resolve(SARAttrs attrs,ClassLoader classLoader){
		String cn=attrs.getProperty(BOOT_CLASS_PROPERTY_NAME);
		if(cn!=null){
			try {
				return classLoader.loadClass(cn);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Property<"+BOOT_CLASS_PROPERTY_NAME+"> error,cause:"+e.getMessage(),e);
			}
		}
		return null;
	}
	
	
}
