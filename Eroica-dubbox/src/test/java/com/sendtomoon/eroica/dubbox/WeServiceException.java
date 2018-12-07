
package com.sendtomoon.eroica.dubbox;



public class WeServiceException extends RuntimeException {

	private static final long serialVersionUID = -1182299763306599962L;

	
	
	public WeServiceException() {
	}

    public WeServiceException(String exceptionMessage) {
        super(exceptionMessage);
    }
}