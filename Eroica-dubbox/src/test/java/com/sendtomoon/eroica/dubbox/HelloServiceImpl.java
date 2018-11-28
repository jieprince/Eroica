
package com.sendtomoon.eroica.dubbox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * ValidationServiceImpl
 * 
 * @author william.liangf
 */
public class HelloServiceImpl implements WeService {
    
    private final AtomicInteger i = new AtomicInteger();

    
	@Override
	public Map<Object,Object> $invoke(Map<Object,Object> params)  {
		Map<Object,Object> result=new HashMap<Object,Object>();
		result.put("result","request: " + params + ", response: " + i.getAndIncrement());
		return result;
	}

}
