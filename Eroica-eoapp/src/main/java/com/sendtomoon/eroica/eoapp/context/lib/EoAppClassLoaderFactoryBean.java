package com.sendtomoon.eroica.eoapp.context.lib;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;

import com.pingan.pafa.pola.ArtifactNotFoundException;
import com.pingan.pafa.pola.Pola;
import com.sendtomoon.eroica.common.utils.EroicaConfigUtils;
import com.sendtomoon.eroica.pizza.classloader.PizzaClassLoader;
import com.sendtomoon.eroica.eoapp.EoAppException;

public class EoAppClassLoaderFactoryBean implements InitializingBean, EoAppClassLoaderFactory {

	private Log logger = LogFactory.getLog(this.getClass());

	private PizzaClassLoader classLoader;

	private Pola pola;

	private String eoappName;

	private String libList;

	private String libDirectory;

	private boolean parentPriority = false;

	public EoAppClassLoaderFactoryBean() {
	}

	@Override
	public void afterPropertiesSet() {
		if (eoappName == null || (eoappName = eoappName.trim()).length() == 0) {
			throw new FatalBeanException("eoappName requried.");
		}
		if (logger.isInfoEnabled()) {
			logger.info("Eoapp<" + eoappName + "> classloaderFactory inited,parentPriority=" + parentPriority
					+ ",libList=" + libList + ",libDirectory=" + libDirectory);
		}
		initClassLoader();
	}

	protected void initClassLoader() {
		loadLocalDependencyLibs();
		loadDependencyLibs();
	}

	protected void loadLocalDependencyLibs() {
		String libDirectory = this.getLibDirectory();
		if (libDirectory == null || (libDirectory = libDirectory.trim()).length() == 0) {
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info("eoapp.lib.dir=" + libDirectory);
		}
		File _libDirectory = new File(libDirectory);
		if (!_libDirectory.exists()) {
			throw new EoAppException("eoapp.lib.dir=" + libDirectory + " not exists.");
		}
		if (!_libDirectory.isDirectory()) {
			throw new EoAppException("eoapp.lib.dir=" + libDirectory + " not be directory.");
		}
		File[] children = _libDirectory.listFiles();
		if (children == null || children.length == 0) {
			return;
		}
		List<File> libFiles = new ArrayList<File>();
		for (File childrenFile : children) {
			if (childrenFile.isFile() && childrenFile.getName().toLowerCase().endsWith(".jar")) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found dependency lib file:" + childrenFile.getAbsolutePath());
				}
				libFiles.add(childrenFile);
			}
		}
		if (libFiles.size() == 0) {
			return;
		}
		PizzaClassLoader classLoader = createClassLoader();
		for (File jarFile : libFiles) {
			try {
				if (logger.isInfoEnabled()) {
					logger.info("Load Dependency lib file:" + jarFile.getName());
				}
				classLoader.addURL(jarFile.toURI().toURL());
			} catch (MalformedURLException e) {
				throw new EoAppException(e.getMessage(), e);
			}
		}
	}

	protected PizzaClassLoader createClassLoader() {
		if (classLoader != null) {
			return classLoader;
		}
		try {
			classLoader = pola.createClassloader("EOAPP#" + eoappName, null, EroicaConfigUtils.getRootClassLoader(),
					parentPriority, false);
		} catch (Exception ex) {
			throw new EoAppException("Create classLoader error,cause:" + ex.getMessage(), ex);
		}
		return classLoader;
	}

	protected void loadDependencyLibs() {
		String libList = this.getLibList();
		if (libList == null || (libList = libList.trim()).length() == 0) {
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info("eoapp.lib.list=" + libList);
		}
		PizzaClassLoader classLoader = createClassLoader();
		Set<String> jarURIs = EroicaConfigUtils.split(libList);
		for (String jarURI : jarURIs) {
			try {
				URL fileURL = pola.getArtifactURL(jarURI);
				if (fileURL != null) {
					classLoader.addURL(fileURL);
				}
			} catch (ArtifactNotFoundException e) {
				throw new EoAppException("Load Dependency<" + jarURI + "> failed,Cause:" + e.getMessage(), e);
			}
		}
	}

	public String getLibList() {
		return libList;
	}

	public void setLibList(String libList) {
		this.libList = libList;
	}

	public PizzaClassLoader getClassLoader() {
		return this.classLoader;
	}

	public String getEoappName() {
		return eoappName;
	}

	public void setEoappName(String eoappName) {
		this.eoappName = eoappName;
	}

	public String getLibDirectory() {
		return libDirectory;
	}

	public void setLibDirectory(String libDirectory) {
		this.libDirectory = libDirectory;
	}

	public boolean isParentPriority() {
		return parentPriority;
	}

	public void setParentPriority(boolean parentPriority) {
		this.parentPriority = parentPriority;
	}

	public Pola getPola() {
		return pola;
	}

	public void setPola(Pola pola) {
		this.pola = pola;
	}

}
