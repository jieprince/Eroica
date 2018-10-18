package com.sendtomoon.eroica.eoapp.sar.lib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.FileSystemUtils;

import com.pingan.pafa.pola.ArtifactNotFoundException;
import com.pingan.pafa.pola.Pola;
import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;
import com.sendtomoon.eroica.pizza.classloader.PizzaClassLoader;
import com.sendtomoon.eroica.eoapp.sar.SARAttrs;
import com.sendtomoon.eroica.eoapp.sar.SARException;

public class SARClassLoaderFactoryBean implements SARClassLoaderFactory{
	
	private Log logger=LogFactory.getLog(this.getClass());
	
	private String sarName;
	
	private Pola pola;
	
	private SARAttrs attrs;
	
	public SARClassLoaderFactoryBean(SARAttrs attrs,Pola pola){
		this.sarName=attrs.getSarName();
		this.attrs=attrs;
		this.pola=pola;
	}
	
	public PizzaClassLoader createClassLoader(ClassLoader parentClassLoader,ServletContext servletContext){
		try{
			String classesJars =attrs.getClassesJars();
			Set<String> classesJarGoordinates=null;
			if(classesJars!=null && classesJars.length()>0){
				classesJarGoordinates=EroicaConfigUtils.split(classesJars);
			}
			//-----------------------------
			PizzaClassLoader  classLoader=pola.createClassloader(sarName, classesJarGoordinates, parentClassLoader
					, attrs.isParentPriority(), attrs.isClassesJarRequried());
			String libList=attrs.getLibList();
			if(libList!=null && libList.length()>0){
				if(logger.isInfoEnabled()){
					logger.info("sar.lib.list="+libList);
				}
				Set<String> jarURIs=EroicaConfigUtils.split(libList);
				for(String jarURI:jarURIs){
					try {
						URL fileURL = pola.getArtifactURL(jarURI);
						if(fileURL!=null) {
							classLoader.addURL(fileURL);
						}
					} catch (ArtifactNotFoundException e) {
						throw new SARException("Load Dependency<"+jarURI+"> failed,cause:"+e.getMessage(),e);
					}
				}
			}
			File classesDirectory=classLoader.getClassesDirectory();
			if(classesDirectory!=null && servletContext!=null) {
				processWebrootFiles(classesDirectory,servletContext);
			}
			return classLoader;
		}catch(Throwable ex){
			throw new SARException("SAR<"+sarName+"> init classloader error:"+ex.getMessage(),ex);
		}
	}
	
	protected void processWebrootFiles(File classesDirectory,ServletContext servletContext){
		File webrootDir=new File(classesDirectory,"webroot");
		if(!(webrootDir.exists() && webrootDir.isDirectory())){
			return ;
		}
		File rootDir=this.resolveDeployTargetDirectory(servletContext);
		//
		File[] srcFiles=webrootDir.listFiles();
		if(srcFiles!=null && srcFiles.length>0){
			for(File srcFile:srcFiles){
				String fileName=srcFile.getName();
				if(fileName.equals("WEB-INF") || fileName.equals("META-INF")){
					continue;
				}
				File destFile=new File(rootDir,fileName);
				if(srcFile.isDirectory()){
					boolean flag=FileSystemUtils.deleteRecursively(destFile);
					if(logger.isDebugEnabled()){
						logger.debug("Delete directory<"+destFile.getAbsolutePath()+"/>,success="+flag);
					}
				}
				try {
					FileSystemUtils.copyRecursively(srcFile, destFile);
				} catch (IOException e) {
					throw new FatalBeanException("Copy web resources files to webroot error,cause:"+e.getMessage(),e);
				}
			}
		}
	}
	
	private File resolveDeployTargetDirectory(ServletContext servletContext){
		String deployRootDirectory=servletContext.getRealPath("/");
		if(logger.isDebugEnabled()){
			logger.info("ServletContext root="+deployRootDirectory);
		}
		if(deployRootDirectory==null || deployRootDirectory.length()==0){
			throw new FatalBeanException("ServletContext root directory not found,please check j2ee config 'unpackWAR=true'.");
		}
		File targetDir=new File(deployRootDirectory);
		boolean flag=targetDir.mkdirs();
		if(logger.isDebugEnabled()){
			logger.debug("Create directory<"+targetDir.getAbsolutePath()+"/>,success="+flag);
		}
		return targetDir;
	}
	
}
