package com.sendtomoon.eroica.pizza.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

import com.sendtomoon.eroica.common.utils.PURL;
import com.sendtomoon.eroica.pizza.PizzaException;
import com.sendtomoon.eroica.pizza.PizzaListener;
import com.sendtomoon.eroica.pizza.PizzaPathListener;
import com.sendtomoon.eroica.pizza.classloader.PizzaURL;

public class ClassPathPizzaManager extends AbstractPizzaMananger {

	private String classpath;

	@Override
	protected void doInit(PURL configURL) {
		String classpath = configURL.getPath();
		if (classpath == null)
			classpath = "";
		String rootLocation = "classpath:" + classpath;
		try {
			System.out.println("Classpath Pizza Manager=" + rootLocation);
			if (classpath.endsWith("/")) {
				classpath = classpath.substring(0, classpath.length() - 1);
			}
			if (classpath.startsWith("/")) {
				classpath = classpath.substring(1);
			}
			this.classpath = classpath;
		} catch (Exception e) {
			throw new PizzaException(
					"Classpath pizza manager configURL error<" + configURL + "> error:" + e.getMessage(), e);
		}
	}

	@Override
	protected boolean doAdd(String path, String content, boolean ephemeral) {
		return true;
	}

	@Override
	protected boolean doDel(String path) {
		return true;
	}

	@Override
	protected String doGet(String path) {
		InputStream content = this.getInputStream(path);
		if (content == null) {
			return null;
		} else {
			try {
				byte[] bytes = IOUtils.toByteArray(content);
				if (PizzaURL.isBase64Content(path)) {
					return Base64.encodeBase64String(bytes);
				} else {
					return new String(bytes, this.getCharset());
				}
			} catch (IOException e) {
				throw new PizzaException(
						"Read resources<" + path + "> error for pizza<" + path + "> :" + e.getMessage(), e);
			}
		}
	}

	protected InputStream getInputStream(String path) {
		ClassPathResource res = new ClassPathResource(this.classpath + path, ClassUtils.getDefaultClassLoader());
		try {
			return res.getInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected boolean doRemoveListener(String path) {
		return true;
	}

	@Override
	protected List<String> doListChildren(String parentPath) {
		return null;
	}

	@Override
	public int doCountChildren(String parentPath) {
		return 0;
	}

	@Override
	protected boolean doCreatePath(String path, boolean ephemeral) {
		return true;
	}

	@Override
	protected void doSet(String path, String content, boolean ephemeral) {

	}

	@Override
	public void doSetListener(String path, PizzaListener listener) {

	}

	@Override
	protected boolean doRemovePathListener(String parentPath) {
		return true;
	}

	@Override
	protected void doSetPathListener(String path, PizzaPathListener listener) {

	}

	@Override
	protected boolean doExists(String path) {
		return getInputStream(path) != null;
	}

	@Override
	protected void doShutdown() {
	}

	@Override
	public String toString() {
		return "classpath:" + classpath;
	}

}
