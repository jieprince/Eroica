package com.sendtomoon.eroica.dubbox;

import java.util.Map;




/**
 * 通用服务接口
 * 
 * @export
 */
public interface WeService {

    
	Map<Object,Object> $invoke(Map<Object,Object> params) throws WeServiceException;

}