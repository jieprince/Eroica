package com.sendtomoon.eroica.eoapp.sar.context;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.web.client.ResourceAccessException;

import com.pingan.pafa.pola.Pola;
import com.sendtomoon.eroica.pizza.spring.PizzaXmlApplicationContext;

public class SARXmlApplicationContext extends PizzaXmlApplicationContext {
	
	private static final String LIB_SUFFIX="lib:";

	private SARResourceResolver sarResourceResolver;
	
	private Pola pola;
	
    @Override
    public Resource getResource(String location) {
       if(pola!=null && location.startsWith(LIB_SUFFIX)){
    	   String artifactURI=location.substring(LIB_SUFFIX.length());
    	   if((artifactURI=artifactURI.trim()).length()>1){
    		   return pola.getResource(artifactURI, false);
    	   }else{
    		   throw new ResourceAccessException("location<"+location+"> format error.");
    	   }
       }else{
    	   location=sarResourceResolver.resolveResourceLocation(location);
    	   if(location!=null)return super.getResource(location);
    	   return null;
       }
      
    }
     
    public Resource[] getResources(String locationPattern)
    throws IOException
    {
    	return sarResourceResolver.resolveResources(locationPattern, this.getResourcePatternResolver());
    }
    


	public SARXmlApplicationContext(String sarName,Resource[] resources,
			Pola pola,String... basePackages) {
		super(sarName,resources);
		this.sarResourceResolver=new SARResourceResolver(sarName,basePackages);
		this.setBasePackages(basePackages);
		this.pola=pola;
	}


	
	

}
