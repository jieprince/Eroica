 package com.sendtomoon.eroica.pizza.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;

public class PizzaClassLoader extends URLClassLoader {
	
	protected Log logger=LogFactory.getLog(this.getClass());
	
	private String name;
	
	private String resourceNamespace;
	
	private URL classpath;
	
	private ClassLoader parent;
	
	private boolean parentPriority=false;
	
	private long createTimestamp;
	
	public PizzaClassLoader(String name,URL classpath,ClassLoader parent){
		super((classpath==null?new URL[]{}:new URL[]{classpath}),parent);
		this.classpath=classpath;
		//--------------------------
		if(name==null || (name=name.trim()).length()==0){
			throw new NullPointerException("name is null");
		}
		this.name=name;
		this.parent=parent;
		if(logger.isDebugEnabled()){
			logger.info("Create pizza classloader<"+name+">,classpath<"+classpath+"> and parent:"+parent);
		}
		createTimestamp=System.currentTimeMillis();
	}
	
	public PizzaClassLoader(String name){
		this(name,null,EroicaConfigUtils.getRootClassLoader());
	}
	
	public PizzaClassLoader(String name,URL classpath){
		this(name,classpath,EroicaConfigUtils.getRootClassLoader());
	}
	

	@Override
	public void addURL(URL url) {
		if(logger.isDebugEnabled()){
			logger.debug(getClass().getSimpleName() + "@"+this.name+"@" +
					new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new java.util.Date(createTimestamp))
					+" add jar URL:"+url);
		}
		super.addURL(url);
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "@"+this.name+"@" +
					new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new java.util.Date(createTimestamp))
					+", and parent="+this.getParent();
	}

	@Override
	public URL findResource(String resourceName) {
		if(PizzaURL.isPizzaURL(resourceName)){
			PizzaURL pizzaURL= new PizzaURL(resourceName);
			return pizzaURL.toJavaURL();
		}
		URL url= super.findResource(resourceName);
		if(url==null){
			return resoulvePizzaResource(resourceName);
		}
		return url;
	}
	
	
	
	@Override
	public URL getResource(String name) {
		if(parent==null || parentPriority){
			return super.getResource(name);
		}else{
			URL url=findResource(name);
			if (url == null) {
				url = parent.getResource(name);
			}
			return url;
		}
	}
	
	 public Enumeration<URL> getResources(String name) throws IOException {
	    if(parent==null || parentPriority){
			return super.getResources(name);
		}
		Enumeration<URL>[] tmp = new Enumeration[2];
		tmp[0] = findResources(name);
		tmp[1] =parent.getResources(name);
		return new CompoundEnumeration<URL>(tmp);
	}


	protected  URL resoulvePizzaResource(String resourceName){
		if(resourceName==null || (resourceName=resourceName.trim()).length()<2){
			return null;
		}
		//只能支持根目录下的
		if(resourceName.indexOf('/')!=-1 || resourceName.indexOf('\\')!=-1){
			return null;
		}
		//不支持class
		if(resourceName.endsWith(".class")){
			return null;
		}
		String namespace=this.getResourceNamespace();
		if(namespace==null){
			namespace=this.getName();
		}
		PizzaURL pizzaURL= ClasspathResourceKey.valueOf(namespace, resourceName).toPizzaURL();
		if(pizzaURL!=null && pizzaURL.exists()){
			if(logger.isInfoEnabled()){
				logger.info("FoundPizzaResource="+pizzaURL);
			}
			//写到classpath下
			if(classpath!=null){
				File classpathDirectory=new File(classpath.getFile());
				File outputFile=new File(classpathDirectory,resourceName);
				try {
					if(logger.isDebugEnabled()){
						logger.debug("Write pizzaURL resource<"+pizzaURL+"> to classpath file<"+outputFile+">.");
					}
					FileUtils.writeByteArrayToFile(outputFile, IOUtils.toByteArray(pizzaURL.getInputStream()));
					return outputFile.toURI().toURL();
				} catch (IOException e) {
					logger.error("Write pizzaURL="+pizzaURL+" to classpath file:"+outputFile+"error,cause:"+e.getMessage(),e);
				}
			}else{
				return pizzaURL.toJavaURL();
			}
		}
		return null;
	}
	
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(name==null) {
			throw new NullPointerException("class name requried.");
		}
		boolean isSupper=false;
		if((parent==null) 
				|| (parent!=null && parentPriority)
				|| name.startsWith("java.") || name.startsWith("javax.")) {
			isSupper=true;
		}
		if(isSupper){
			return super.loadClass(name, resolve);
		}else{
			// First, check if the class has already been loaded
			Class<?> c = findLoadedClass(name);
			if (c == null) {
				try {
					// to find the class.
			        c = findClass(name);
				} catch (ClassNotFoundException e) {
	                // ClassNotFoundException thrown if class not found
				}catch(SecurityException e) {
				}
				if(c==null){
					c = parent.loadClass(name);
				}
			}
			if (resolve) {
			    resolveClass(c);
			}
			return c;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResourceNamespace() {
		return resourceNamespace;
	}

	public void setResourceNamespace(String resourceNamespace) {
		this.resourceNamespace = resourceNamespace;
	}

	public boolean isParentPriority() {
		return parentPriority;
	}

	public void setParentPriority(boolean parentPriority) {
		this.parentPriority = parentPriority;
	}

	public File getClassesDirectory() {
		return (classpath==null?null:new File(classpath.getFile()));
	}
	
}

class CompoundEnumeration<E> implements Enumeration<E> {
	private Enumeration<E>[] enums;
	private int index = 0;

	public CompoundEnumeration(Enumeration<E>[] enums) {
		this.enums = enums;
	}

	private boolean next() {
		while (this.index < this.enums.length) {
			if ((this.enums[this.index] != null) && (this.enums[this.index].hasMoreElements())) {
				return true;
			}
			this.index += 1;
		}
		return false;
	}

	public boolean hasMoreElements() {
		return next();
	}

	public E nextElement() {
		if (!next()) {
			throw new NoSuchElementException();
		}
		return this.enums[this.index].nextElement();
	}
}