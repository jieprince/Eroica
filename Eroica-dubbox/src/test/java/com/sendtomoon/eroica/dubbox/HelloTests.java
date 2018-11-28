
package com.sendtomoon.eroica.dubbox;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;

/**
 * CacheTest
 * 
 * @author william.liangf
 */
public class HelloTests extends TestCase {

    @Test
    public void testCache() throws Exception {
    	RegistryConfig registry=new RegistryConfig("zookeeper://127.0.0.1:2181");
        ServiceConfig<WeService> service = new ServiceConfig<WeService>();
        service.setApplication(new ApplicationConfig("test-provider"));
        service.setRegistry(registry);
        service.setProtocol(new ProtocolConfig("dubbo", 29582));
        service.setRef(new HelloServiceImpl());
        service.setId("test/hello"); 
        service.setInterface(WeService.class);
        service.setPath("hello");
       // service.setAccesslog(true);
        service.export();
        try {
            ReferenceConfig<WeService> reference = new ReferenceConfig<WeService>();
            reference.setApplication(new ApplicationConfig("test-consumer"));
            reference.setRegistry(registry);
           // reference.setScope("remote");
            reference.setInterface(WeService.class);
            reference.setId("test/hello");
            //reference.setUrl("dubbo://127.0.0.1:29582");
         
            WeService s = reference.get();
            try {
            	Map<Object,Object> params=new HashMap<Object,Object>();
            	params.put("id","0");
            	  Object result =s.$invoke(params);
            	  System.out.println("------"+result);
            }catch(Exception ex){
            	ex.printStackTrace();
            } finally {
                reference.destroy();
                System.out.println("------");
            }
        } finally {
            service.unexport();
            System.out.println("------");
        }
    }

}
