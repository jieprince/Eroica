package com.sendtomoon.eroica.dubbo.ws;

import java.util.Map;

import javax.servlet.ServletException;




/**
 * 服务接口
 * 
 * @export
 */
public interface IWeService {
	
	
	Map<Object,Object> $invoke(Map<Object,Object> params) throws ServletException;  

}