package com.sendtomoon.eroica.eoapp.sar;

import com.sendtomoon.eroica.eoapp.EoAppException;

public class SARException extends EoAppException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6144475420830258875L;

	public SARException(String msg){
		super(msg);
	}
	
	public SARException(String msg,Throwable th){
		super(msg,th);
	}

}
