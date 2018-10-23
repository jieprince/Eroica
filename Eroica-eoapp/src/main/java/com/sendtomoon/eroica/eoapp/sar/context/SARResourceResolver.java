package com.sendtomoon.eroica.eoapp.sar.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

public class SARResourceResolver  {
	
	public static String SAR_URL_PREFIX = "sar:";
	
	public volatile String[] basePaths=null;
	
	private volatile String sarName;
	
	public SARResourceResolver(String sarName,String basePackages[]){
		this.basePaths=this.getBasePath(basePackages);
		this.sarName=sarName;
	}
	
	public  String resolveResourceLocation(String location){
		if(location.startsWith(SAR_URL_PREFIX)){
			if(basePaths==null){
				return ResourceLoader.CLASSPATH_URL_PREFIX+"META-INF/"+sarName+"/"
						+location.substring(SAR_URL_PREFIX.length());
			}else{
				return ResourceLoader.CLASSPATH_URL_PREFIX+basePaths[0]
						+location.substring(SAR_URL_PREFIX.length());
			}
			
		}else {
			return location;
		}
		
	}
	
	public  Resource[] resolveResources(String locationPattern
			,ResourcePatternResolver resourcePatternResolver)
		throws IOException{
		if(!locationPattern.startsWith(SAR_URL_PREFIX)){
			return resourcePatternResolver.getResources(locationPattern);
		}
		if(basePaths==null){
			return  new Resource[0];
		}
		if(basePaths.length==1){
			return resourcePatternResolver.getResources(
					ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX+basePaths[0]
					+locationPattern.substring(SAR_URL_PREFIX.length()));
		}else{
			List<Resource> datas=null;
			for(String basePath:basePaths){
				Resource[] tempResources=resourcePatternResolver.getResources(
						ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX+basePath
    					+locationPattern.substring(SAR_URL_PREFIX.length()));
				if(tempResources!=null && tempResources.length>0){
					if(datas==null){
						datas=new ArrayList<Resource>();
					}
					for(Resource tempResource:tempResources){
						datas.add(tempResource);
					}
				}
			}
			if(datas!=null){
				Resource[] arr=new Resource[datas.size()];
				return datas.toArray(arr);
			}
			return new Resource[]{};
		}
	}
	
	protected  String[] getBasePath(String basePackages[]){
		if(basePackages==null || basePackages.length==0){
			return null;
		}
		List<String> basePaths=null;
		for(String classpath:basePackages){
			if(classpath!=null && (classpath=classpath.trim()).length()>0){
				char[] array=classpath.toCharArray();
				for(int i=0;i<array.length;i++){
					if(array[i]=='.'){
						array[i]='/';
					}
				}
				String basePath=new String(array);
				if(!basePath.endsWith("/")){
					basePath=basePath+"/";
				}
				if(basePaths==null){
					basePaths=new ArrayList<String>();
				}
				basePaths.add(basePath);
			}
		}
		if(basePaths!=null && basePaths.size()>0){
			String[] result=new String[basePaths.size()];
			basePaths.toArray(result);
			return result;
		}
		return null;
	}


	
	
}
