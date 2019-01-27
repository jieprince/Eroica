package com.sendtomoon.eroica.pizza.support.html;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.context.ServletContextAware;

import com.pingan.pafa.pola.ArtifactNotFoundException;
import com.pingan.pafa.pola.Pola;
import com.sendtomoon.eroica.common.exception.EroicaException;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;

public class HtmlDeployBean implements ServletContextAware, InitializingBean {

	private volatile boolean inited;

	private boolean forceClean = true;

	private Log logger = LogFactory.getLog(this.getClass());

	private String deployPath;

	private String deployRootDirectory;

	private PizzaURL pizzaURL;

	private boolean requiredExists = false;

	@Autowired
	private Pola pola;

	public HtmlDeployBean() {
	}

	protected void handleContent(final URL zipURL) {
		if (zipURL == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("PizzaResource<" + this.getPizzaURL() + "> be empty or not exists.");
			}
			return;
		}
		final String deployRootDirectory = this.getDeployRootDirectory();
		File localZipFile = new File(zipURL.getFile());
		try {
			File targetDirectory = resolveDeployTargetDirectory(deployRootDirectory);
			try {
				unzipFile(targetDirectory, localZipFile);
				if (logger.isInfoEnabled()) {
					logger.info("Un-zip file<" + this.getPizzaURL().getPizzaKey() + "> to directory<"
							+ targetDirectory.getAbsolutePath() + "> completed.");
				}
			} finally {
				localZipFile.delete();
			}
		} catch (Exception e) {
			throw new EroicaException("Un zip file<" + this.getPizzaURL() + "> error:" + e.getMessage(), e);
		}
	}

	protected File resolveDeployTargetDirectory(final String deployRootDirectory) {
		if (deployRootDirectory == null || deployRootDirectory.length() == 0) {
			throw new EroicaException("Not inited by ServletContext for key<" + this.getPizzaURL() + ">.");
		}
		String deployPath = this.getDeployPath();
		File targetDir = null;
		if (deployPath != null) {
			targetDir = new File(deployRootDirectory, deployPath);
		} else {
			targetDir = new File(deployRootDirectory);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Un-zip file<" + this.getPizzaURL() + "> to directory<" + targetDir.getAbsolutePath() + ">...");
		}
		if (this.isForceClean() && deployPath != null) {
			boolean flag = FileSystemUtils.deleteRecursively(targetDir);
			if (logger.isDebugEnabled()) {
				logger.debug("Delete directory<" + targetDir.getAbsolutePath() + "/>,success=" + flag);
			}
		}
		//
		boolean flag = targetDir.mkdirs();
		if (logger.isDebugEnabled()) {
			logger.debug("Create directory<" + targetDir.getAbsolutePath() + "/>,success=" + flag);
		}
		return targetDir;
	}

	protected void unzipFile(File targetDirectory, File zipFile) throws IOException {
		BufferedInputStream zipFileInput = new BufferedInputStream(new FileInputStream(zipFile));
		ZipInputStream zipInput = new ZipInputStream(zipFileInput);
		ZipFile _zipFile = null;
		try {
			ZipEntry entry = null;
			_zipFile = new ZipFile(zipFile);
			while ((entry = (ZipEntry) zipInput.getNextEntry()) != null) {
				unzipFileEntry(targetDirectory, _zipFile, entry);
			}
		} finally {
			IOUtils.closeQuietly(zipFileInput);
			IOUtils.closeQuietly(zipInput);
			if (_zipFile != null) {
				_zipFile.close();
			}
		}
	}

	protected void unzipFileEntry(File targetDirectory, ZipFile zipFile, ZipEntry entry) throws IOException {
		String fileName = entry.getName();
		if (entry.isDirectory()) {
			return;
		}
		File entryFile = new File(targetDirectory, File.separator + fileName);
		if (entryFile.exists()) {
			entryFile.delete();
		}
		boolean flag = true;
		try {
			entryFile.getParentFile().mkdirs();
			flag = entryFile.createNewFile();
		} catch (Exception ex) {
			throw new EroicaException("Create File failed:" + entryFile.getAbsolutePath(), ex);
		}
		//
		BufferedOutputStream entryFileOutput = new BufferedOutputStream(new FileOutputStream(entryFile));
		BufferedInputStream entryFileInput = new BufferedInputStream(zipFile.getInputStream(entry));
		int size = -1;
		try {
			size = IOUtils.copy(entryFileInput, entryFileOutput);
			entryFileOutput.flush();
		} finally {
			IOUtils.closeQuietly(entryFileInput);
			IOUtils.closeQuietly(entryFileOutput);
		}
		if (logger.isInfoEnabled()) {
			logger.info("Created file<" + fileName + ">,success=" + flag + ",file size=" + size + " bytes.");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init(servletContext);
	}

	private ServletContext servletContext;

	public synchronized void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
		init(servletContext);
	}

	public synchronized void init(ServletContext servletContext) {
		if (servletContext == null)
			return;
		if (inited) {
			return;
		} else {
			inited = true;
		}
		String deployPath = this.getDeployPath();
		if (deployPath == null || (deployPath = deployPath.trim()).length() == 0 || deployPath.equals("/")) {
			deployPath = null;
		} else {
			if (deployPath.charAt(0) != '/') {
				deployPath = '/' + deployPath;
			}
		}
		this.deployPath = deployPath;
		//
		String deployRootDirectory = this.getDeployRootDirectory();
		if (deployRootDirectory == null || deployRootDirectory.length() == 0) {
			deployRootDirectory = servletContext.getRealPath("/");
			if (logger.isInfoEnabled()) {
				logger.info("ServletContext root=" + deployRootDirectory);
			}
			if (deployRootDirectory == null) {
				throw new FatalBeanException(
						"ServletContext root directory not found,please check tomcat config 'unpackWAR=true'.");
			}
			this.setDeployRootDirectory(deployRootDirectory);
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("deployRootDirectory=" + deployRootDirectory);
			}
		}
		PizzaURL pizzaURL = this.getPizzaURL();
		if (pizzaURL == null) {
			throw new FatalBeanException("pizzaURL requried");
		}
		URL zipURL = null;
		try {
			zipURL = pola.getArtifactURL(pizzaURL.getPizzaKey());
		} catch (ArtifactNotFoundException ex) {
			if (this.isRequiredExists()) {
				throw new EroicaException(ex.getMessage(), ex);
			}
		}
		// -----------------------------
		handleContent(zipURL);
	}

	public PizzaURL getPizzaURL() {
		return pizzaURL;
	}

	public void setPizzaURL(PizzaURL pizzaURL) {
		this.pizzaURL = pizzaURL;
	}

	public void setPizzaURL(String pizzaURL) {
		this.pizzaURL = PizzaURL.valueOf(pizzaURL);
	}

	public String getDeployPath() {
		return deployPath;
	}

	public void setDeployPath(String deployPath) {
		this.deployPath = deployPath;
	}

	public String getDeployRootDirectory() {
		return deployRootDirectory;
	}

	public void setDeployRootDirectory(String deployRootDirectory) {
		this.deployRootDirectory = deployRootDirectory;
	}

	public boolean isForceClean() {
		return forceClean;
	}

	public void setForceClean(boolean forceClean) {
		this.forceClean = forceClean;
	}

	/***
	 * @deprecated
	 * @return
	 */
	public boolean isListenEnable() {
		return false;
	}

	/***
	 * @deprecated
	 * @return
	 */
	public void setListenEnable(boolean listenEnable) {

	}

	public boolean isRequiredExists() {
		return requiredExists;
	}

	public void setRequiredExists(boolean requiredExists) {
		this.requiredExists = requiredExists;
	}

	public Pola getPola() {
		return pola;
	}

	public void setPola(Pola pola) {
		this.pola = pola;
	}

}
