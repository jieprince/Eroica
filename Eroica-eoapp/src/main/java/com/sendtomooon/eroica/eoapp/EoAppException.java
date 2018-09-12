package com.sendtomooon.eroica.eoapp;

public class EoAppException extends RuntimeException {

	private static final long serialVersionUID = 4642126793042527563L;

	public EoAppException(String msg) {
		super(msg);
	}

	public EoAppException(String msg, Throwable th) {
		super(msg, th);
	}

}
