package com.sendtomoon.eroica.eoapp.sar;

import java.util.Properties;

public class SARAttrs extends AbstractSARAttrs{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//组件classes包
	/***
	 * @deprecated KEY_CLASSES_KEY
	 */
	private final static String KEY_LIB_KEY="sar.lib";
	
	private final static String KEY_CLASSES_KEY="sar.classes";
	
	
	private final static  String KEY_CLASSLOADER_PARENT_PRIORITY="sar.classloader.parent.priority";

	/***
	 * @deprecated KEY_LIB_LIST
	 */
	private final static String KEY_EX_LIB_LIST="sar.ex-lib.list";
	
	private final static String KEY_LIB_LIST="sar.lib.list";
	
	
	private String sarName=null;
	
	
	private String libList;
	
	private String classesJars;
	
	private boolean classesJarRequried=true;
	
	private boolean parentPriority;
	
	public SARAttrs(String sarName,Properties properties){
		super(properties,null);
		this.sarName=sarName;
		this.classesJars=getProperty(KEY_CLASSES_KEY);
		if(classesJars==null){
			this.classesJars=getProperty(KEY_LIB_KEY);
		}
		if(classesJars==null){
			classesJarRequried=false;
			this.classesJars=sarName+".jar";
		}
		this.parentPriority=getProperty(KEY_CLASSLOADER_PARENT_PRIORITY,false);
		this.libList=getProperty(KEY_LIB_LIST);
		if(this.libList==null){
			libList=getProperty(KEY_EX_LIB_LIST);
		}
	}
	
	
	
	
	
	public String getSarName(){
		return sarName;
	}
	
	


	public boolean isParentPriority() {
		return parentPriority;
	}





	public String getLibList() {
		return libList;
	}





	public String getClassesJars() {
		return classesJars;
	}





	public boolean isClassesJarRequried() {
		return classesJarRequried;
	}





	
	
	
}
