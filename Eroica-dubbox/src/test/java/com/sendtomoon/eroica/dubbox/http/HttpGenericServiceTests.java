
package com.sendtomoon.eroica.dubbox.http;

import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.sendtomoon.eroica.ac.dubbo.GenericParam;
import com.sendtomoon.eroica.dubbox.GenericServiceSample;

/**
 * CacheTest
 * 
 * @author william.liangf
 */
public class HttpGenericServiceTests extends TestCase {

    @Test
    public void testCache() throws Exception {
    	
    	RegistryConfig registryConfig=new RegistryConfig("zookeeper://127.0.0.1:2181");
        ServiceConfig<GenericService> service = new ServiceConfig<GenericService>();
        service.setApplication(new ApplicationConfig("test-provider"));
        service.setRegistry(registryConfig);
        service.setProtocols(Arrays.asList(new ProtocolConfig("dubbo", 29582),new ProtocolConfig("hessian", 29581)));
        service.setRef(new GenericServiceSample());
        service.setId("gs/hello"); 
        //service.setAccesslog(true);
        service.setRetries(0);
        service.setTimeout(100000);
        service.setInterface(GenericService.class);
        service.export();
        
        ExtensionLoader<Protocol> loader = ExtensionLoader.getExtensionLoader(Protocol.class);
        for (String protocolName : loader.getLoadedExtensions()) {
        	System.err.println("name="+protocolName);
        }
        try {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
            reference.setApplication(new ApplicationConfig("test-consumer"));
            reference.setRegistry(registryConfig);
            reference.setScope("remote");
            reference.setInterface(GenericService.class);
            reference.setId("gs/hello");
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
