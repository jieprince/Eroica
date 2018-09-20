package com.pingan.pafa.pola;

public class ArtifactNotFoundException extends PolaException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ArtifactNotFoundException(String msg){
		super(msg);
	}
	
	public ArtifactNotFoundException(String msg,Throwable th){
		super(msg,th);
	}

}
