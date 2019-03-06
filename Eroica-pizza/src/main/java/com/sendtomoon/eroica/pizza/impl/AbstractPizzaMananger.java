package com.sendtomoon.eroica.pizza.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sendtomoon.eroica.common.utils.PURL;
import com.sendtomoon.eroica.pizza.ConfigChangedListener;
import com.sendtomoon.eroica.pizza.PizzaConstants;
import com.sendtomoon.eroica.pizza.PizzaListener;
import com.sendtomoon.eroica.pizza.PizzaManager;
import com.sendtomoon.eroica.pizza.PizzaPathListener;
import com.sendtomoon.eroica.pizza.classloader.PizzaURLException;

public abstract class AbstractPizzaMananger implements PizzaManager {

	protected Log logger = LogFactory.getLog(this.getClass());

	private volatile LocalBackup localBackup;

	private volatile String defRootPath;

	private volatile Charset charset;

	protected PURL configURL;

	private volatile IGlobalVariablesHandler globalVariablesHandler;

	public AbstractPizzaMananger() {
	}

	@Override
	public final void init(PURL configURL) {
		this.configURL = configURL;
		String rootPath = configURL.getParameter("rootPath");
		boolean isLocalBackup = configURL.getParameter("localBackup", true);
		setDefRootPath(rootPath, true);
		charset = Charset.forName(configURL.getParameter("charset", "UTF-8"));
		// ------------------------------------
		this.doInit(configURL);
		if (isLocalBackup) {
			localBackup = new LocalBackup(configURL, charset);
		}
	}

	public final synchronized void initGlobalVariablesHandler(String path) {
		if (globalVariablesHandler != null) {
			return;
		}
		if (path != null && path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		globalVariablesHandler = new GlobalVariablesHandler();
		globalVariablesHandler.init(path, this);
	}

	public final synchronized void setDefRootPath(String rootPath, boolean forceOverride) {
		//
		if (rootPath == null) {
			return;
		}
		if (this.defRootPath != null && !forceOverride) {
			return;
		}
		rootPath = rootPath.trim();
		if (rootPath.length() > 0 && rootPath.charAt(0) != '/') {
			rootPath = "/" + rootPath;
		}
		if (rootPath.length() == 0 || rootPath.equals("/")) {
			rootPath = "";
		}
		this.defRootPath = rootPath;
	}

	private String getDefaultRootPath() {
		return PizzaConstants.DEF_ROOT_PATH;
	}

	protected abstract void doInit(PURL configURL);

	private static final String PIZZA_PATH_PATTERN_STRING = "^/[\\w\\-\\.]+(/[\\w\\-\\.\\#]+)*$";

	private static final Pattern PIZZA_PATH_PATTERN = Pattern.compile(PIZZA_PATH_PATTERN_STRING);

	protected final static void checkPizzaPath(String pizzaPath) {
		if (pizzaPath == null) {
			throw new PizzaURLException("pizzaPath is null.");
		}
		if (!PIZZA_PATH_PATTERN.matcher(pizzaPath).matches()) {
			throw new PizzaURLException("PizzaPath<" + pizzaPath + "> format error,Not matched by regex="
					+ PIZZA_PATH_PATTERN_STRING + ".");
		}
	}

	@Override
	public boolean add(String group, String key, String content) {
		return add(group + "/" + key, content);
	}

	@Override
	public boolean add(String path, String content) {
		return add(path, content, false);
	}

	@Override
	public boolean add(String path, String content, boolean ephemeral) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		checkPizzaPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("Add path<" + path + "> and ephemeral=" + ephemeral + ", content size="
					+ (content == null ? 0 : content.length()));
		}
		return doAdd(path, content, ephemeral);
	}

	protected abstract boolean doAdd(String path, String content, boolean ephemeral);

	@Override
	public boolean del(String path) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		checkPizzaPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("Del path<" + path + ">.");
		}
		return doDel(path);
	}

	protected abstract boolean doDel(String path);

	@Override
	public boolean del(String group, String key) {
		return this.del(group + "/" + key);
	}

	@Override
	public boolean forceDel(String path) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		checkPizzaPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("ForceDel path<" + path + ">.");
		}
		return doForceDel(path);
	}

	protected boolean doForceDel(String path) {
		List<String> children = this.listChildren(path);
		if (children != null && children.size() > 0) {
			for (String child : children) {
				doForceDel(path + "/" + child);
			}
		}
		return doDel(path);
	}

	@Override
	public boolean createPath(String path) {
		return createPath(path, false);
	}

	public boolean createPath(String path, boolean ephemeral) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		checkPizzaPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("Create path<" + path + ">.");
		}
		return doCreatePath(path, ephemeral);
	}

	protected abstract boolean doCreatePath(String path, boolean ephemeral);

	@Override
	public String get(String path) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		String content = doGet(path);
		if (logger.isInfoEnabled()) {
			logger.info("ReadPizzaResource <" + path + ">, content size=" + (content == null ? 0 : content.length()));
		}
		if (logger.isDebugEnabled() && content != null && content.length() > 0 && content.length() < 10240) {
			logger.debug("ReadPizzaResource <" + path + ">, content:\n" + content + "");
		}
		if (globalVariablesHandler != null) {
			String lowerCasePath = path.toLowerCase();
			// 全局变量过滤
			if (lowerCasePath.endsWith(".properties")) {
				content = globalVariablesHandler.handle(path, content);
			}
		}
		if (localBackup != null) {
			localBackup.pushItem(new LocalBackupFile(path, content));
		}
		return content;
	}

	protected abstract String doGet(String path);

	@Override
	public String get(final String group, final String key) {
		return get(group + "/" + key);
	}

	@Override
	public boolean removeListener(String group, String key, PizzaListener listener) {
		return this.removeListener(group + "/" + key);
	}

	@Override
	public void setListener(String group, String key, ConfigChangedListener listener) {
		this.setListener(group, key, (PizzaListener) listener);
	}

	@Override
	public boolean removeListener(String path) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		if (logger.isInfoEnabled()) {
			logger.info("RemoveListener for pizza <" + path + ">.");
		}
		return doRemoveListener(path);
	}

	@Override
	public boolean removePathListener(String parentPath) {
		if (parentPath.charAt(0) != '/') {
			parentPath = getDefRootPath() + "/" + parentPath;
		}
		if (logger.isInfoEnabled()) {
			logger.info("RemovePathListener for pizza <" + parentPath + ">.");
		}
		return doRemovePathListener(parentPath);
	}

	protected abstract boolean doRemovePathListener(String parentPath);

	@Override
	public boolean removeListener(String group, String key) {
		return this.removeListener(group + "/" + key);
	}

	protected abstract boolean doRemoveListener(String path);

	@Override
	public int countChildren(String parentPath) {
		if (parentPath.charAt(0) != '/') {
			parentPath = getDefRootPath() + "/" + parentPath;
		}
		return doCountChildren(parentPath);
	}

	public abstract int doCountChildren(String parentPath);

	@Override
	public List<String> listChildren(String parentPath, String likeChild, int beginIndex, int maxRecords) {
		if (parentPath.charAt(0) != '/') {
			parentPath = getDefRootPath() + "/" + parentPath;
		}
		List<String> children = doListChildren(parentPath);
		if (children == null || children.size() == 0) {
			return children;
		}
		if (likeChild != null && (likeChild = likeChild.trim()).length() > 0) {
			List<String> selectedChildren = new ArrayList<String>();
			for (String child : children) {
				if (child.indexOf(likeChild) != -1) {
					selectedChildren.add(child);
				}
			}
			children = selectedChildren;
		}
		if (beginIndex < 0) {
			return children;
		}
		if (beginIndex >= children.size()) {
			return null;
		} else if (beginIndex == 0 && maxRecords >= children.size()) {
			return children;
		} else {
			int idx = beginIndex + maxRecords;
			if (idx > children.size()) {
				idx = children.size();
			}
			List<String> list = new ArrayList<String>();
			for (int i = beginIndex; i < idx; i++) {
				String child = children.get(i);
				list.add(child);
			}
			return list;
		}

	}

	@Override
	public List<String> searchKeys(String parentPath, String likeChild, int beginIndex, int maxRecords) {
		return listChildren(parentPath, likeChild, beginIndex, maxRecords);
	}

	@Override
	public List<String> listChildren(String parentPath, String likeChild) {
		return listChildren(parentPath, likeChild, -1, -1);
	}

	@Override
	public List<String> listChildren(String parentPath) {
		return listChildren(parentPath, null, -1, -1);
	}

	protected abstract List<String> doListChildren(String parentPath);

	@Override
	public void set(String group, String key, String content) {
		set(group + "/" + key, content);
	}

	@Override
	public void set(String path, String content) {
		set(path, content, false);
	}

	@Override
	public void set(String path, String content, boolean ephemeral) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		checkPizzaPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("Set path<" + path + "> and ephemeral=" + ephemeral + ", content size="
					+ (content == null ? 0 : content.length()));
		}
		doSet(path, content, ephemeral);
	}

	protected abstract void doSet(String path, String content, boolean ephemeral);

	@Override
	public void setListener(String group, String key, PizzaListener listener) {
		setListener(group + "/" + key, listener);
	}

	@Override
	public void setPathListener(String path, PizzaPathListener listener) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		checkPizzaPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("RegisterPathListener for path <" + path + ">.");
		}
		doSetPathListener(path, listener);
	}

	protected abstract void doSetPathListener(String path, PizzaPathListener listener);

	@Override
	public void setListener(String path, final PizzaListener listener) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		final String fullPath = path;
		checkPizzaPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("RegisterListener for pizza <" + path + ">.");
		}
		if (globalVariablesHandler != null && path.endsWith(".properties")) {
			PizzaListener newListener = new PizzaListener() {

				@Override
				public void handleConfigChange(String pizzaContent) {
					pizzaContent = globalVariablesHandler.handle(fullPath, pizzaContent);
					if (localBackup != null && pizzaContent != null && pizzaContent.length() > 0) {
						localBackup.pushItem(new LocalBackupFile(fullPath, pizzaContent));
					}
					listener.handleConfigChange(pizzaContent);
				}
			};
			doSetListener(path, newListener);
		} else {
			doSetListener(path, listener);
		}
	}

	public abstract void doSetListener(String path, PizzaListener listener);

	@Override
	public boolean exists(String group, String key) {
		return exists(group + "/" + key);
	}

	@Override
	public boolean exists(String path) {
		if (path.charAt(0) != '/') {
			path = getDefRootPath() + "/" + path;
		}
		checkPizzaPath(path);
		boolean exists = doExists(path);
		if (logger.isInfoEnabled()) {
			logger.info("ExistsCheck <" + path + "> = " + exists);
		}
		return exists;
	}

	protected abstract boolean doExists(String path);

	@Override
	public synchronized void shutdown() {
		if (globalVariablesHandler != null) {
			IGlobalVariablesHandler i = this.globalVariablesHandler;
			this.globalVariablesHandler = null;
			i.shutdown();
		}
		doShutdown();
	}

	protected String getDefRootPath() {
		return defRootPath == null ? this.getDefaultRootPath() : defRootPath;
	}

	protected abstract void doShutdown();

	public PURL getConfigURL() {
		return configURL;
	}

	public Charset getCharset() {
		return charset;
	}

}
