package com.sendtomoon.eroica.eoapp.sar.lib;

import javax.servlet.ServletContext;

import com.sendtomoon.eroica.pizza.classloader.PizzaClassLoader;

public interface SARClassLoaderFactory {

	PizzaClassLoader createClassLoader(ClassLoader parentClassLoader,ServletContext servletContext);
	
}
