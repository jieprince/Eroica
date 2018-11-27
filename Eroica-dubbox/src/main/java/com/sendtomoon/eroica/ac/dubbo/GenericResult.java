package com.sendtomoon.eroica.ac.dubbo;

import java.util.Map;

public class GenericResult implements java.io.Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Map<?,?> result;
	
	public GenericResult(){}
	
	public GenericResult(Map<?,?> result){
		this.result=result;
	}

	public Map<?,?> getResult() {
		return result;
	}

	public void setResult(Map<?,?> result) {
		this.result = result;
	}
	
}
