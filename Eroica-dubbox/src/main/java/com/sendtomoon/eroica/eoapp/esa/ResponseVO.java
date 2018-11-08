package com.sendtomoon.eroica.eoapp.esa;

public class ResponseVO {

	protected String responseCode ;
    
    protected String responseMsg;
    
    public ResponseVO(){
    	this.setResponseCode("0");
    }
    
    public ResponseVO(String responseCode){
    	this.setResponseCode(responseCode);
    }
    
    public ResponseVO(String responseCode,String responseMsg){
    	this.setResponseCode(responseCode);
    	this.setResponseMsg(responseMsg);
    }
    

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }
	

}
