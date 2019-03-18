package com.sendtomoon.eroica.pizza.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Set;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

public class GlobalVariablesConvertUtils {
	
	private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("@{", "}", ":", false);
	
	private static final PlaceholderResolver placeholderResolver=new PlaceholderResolver(){
		@Override
		public String resolvePlaceholder(String key) {
			
			return System.getProperty(key);
		}
		
	};

	public static String convert(final String originalContent)
		throws Exception{
		//---------------
		Properties properties=new Properties();
		//------------------------------------------------
		properties.load(new StringReader(originalContent));
		//
		Set<String> keys=properties.stringPropertyNames();
		int count=0;
		for(String key:keys){
			String value=properties.getProperty(key);
			if(value!=null && value.length()>5){
				String newValue=helper.replacePlaceholders(value, placeholderResolver);
				if(!newValue.equals(value)){
					count++;
					properties.put(key, newValue);
				}
			}
		}
		if(count!=0){
			StringWriter out=new StringWriter(512);
			properties.store(out, "Rewrited by GlobalVariables.");
			return out.toString();
		}else{
			return originalContent;
		}
		
	}
}
