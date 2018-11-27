
package com.sendtomoon.eroica.dubbox.monitor;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.dubbox.GenericServiceSample;

/**
 * CacheTest
 * 
 * @author william.liangf
 */
public class MonitorServiceTests extends TestCase {

    @Test
    public void testCache() throws Exception {
    	//System.setProperty("dubbo.monitor.protocol", "registry");
    	RegistryConfig registryConfig=new RegistryConfig("zookeeper://127.0.0.1:2181");
    	//------------
    	 ServiceConfig<MonitorService> ms = new ServiceConfig<MonitorService>();
    	 ms.setApplication(new ApplicationConfig("test-provider"));
    	 ms.setRegistry(registryConfig);
    	 ms.setProtocol(new ProtocolConfig("dubbo", 29582));
    	 ms.setRef(new MonitorServiceSample());
    	 ms.setInterface(MonitorService.class);
    	 ms.export();
        //
        ServiceConfig<GenericService> service = new ServiceConfig<GenericService>();
        service.setApplication(new ApplicationConfig("test-provider"));
        service.setRegistry(registryConfig);
        service.setProtocol(new ProtocolConfig("dubbo", 29582));
        service.setRef(new GenericServiceSample());
        service.setId("gs/hello"); 
        //service.setAccesslog(true);
        service.setRetries(0);
        service.setTimeout(100000);
        service.setInterface(GenericService.class);
        service.export();
        try {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
            reference.setApplication(new ApplicationConfig("test-consumer"));
            reference.setRegistry(registryConfig);
           // reference.setScope("remote");
            reference.setInterface(GenericService.class);
           // reference.setMonitor("dubbo://10.6.160.67:29582");
            reference.setId("gs/hello");
           // reference.setUrl("dubbo://127.0.0.1:29582");
         
            GenericService s = reference.get();
            try {
            	HashMap<Object,Object> params=new HashMap<Object,Object>();
            	params.put("name", "nangua");
            	String result = (String)s.$invoke("handleRequest"
            				,new String[]{HashMap.class.getName()}
            				,new Object[]{params});
            	  System.out.println("------"+result);
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
