
package com.sendtomoon.eroica.dubbox;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.ac.dubbo.GenericParam;

/**
 * CacheTest
 * 
 * @author william.liangf
 */
public class GenericServiceTests extends TestCase {

    @Test
    public void testCache() throws Exception {
    	//System.setProperty("dubbo.monitor.protocol", "registry");
    	RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();
    	Registry registry = registryFactory.getRegistry(URL.valueOf("zookeeper://127.0.0.1:2181"));
    	registry.register(URL.valueOf("script://0.0.0.0/gs/hello?category=routers&dynamic=true&rule=" 
    				+ URL.encode("function route(invokers,model){"
    						+ "java.lang.System.err.println('invokers[0]='"
    						+ "+invokers.get(0).getUrl());return invokers;} route(invokers,model);") ));
    	
    	RegistryConfig registryConfig=new RegistryConfig("zookeeper://10.20.26.69:2181");
        ServiceConfig<GenericService> service = new ServiceConfig<GenericService>();
        service.setApplication(new ApplicationConfig("test-provider"));
        service.setRegistry(registryConfig);
        service.setProtocol(new ProtocolConfig("dubbo", 29582));
        service.setRef(new GenericServiceSample());
        service.setId("gs.hello-abc"); 
        service.setGroup("test.abc");
        //service.setAccesslog(true);
        service.setRetries(0);
        service.setTimeout(100000);
        service.setInterface(GenericService.class);
        //service.setAsync(true);
        service.export();
        try {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
            reference.setApplication(new ApplicationConfig("test-consumer"));
            reference.setRegistry(registryConfig);
            reference.setScope("remote");
            reference.setInterface(GenericService.class);
            reference.setId("gs.hello-abc");
            reference.setGroup("test.abc");
           // reference.setUrl("dubbo://127.0.0.1:29582");
         
            GenericService s = reference.get();
            try {
            	GenericParam params=new GenericParam();
            	HashMap<Object,Object> map=new HashMap<Object,Object>();
            	params.setParams(map);
            	map.put("name", "nangua");
            	String result = (String)s.$invoke("handleRequest"
            				,new String[]{GenericParam.class.getName()}
            				,new Object[]{params});
            	  System.out.println("------"+result);
            	  System.in.read();
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
