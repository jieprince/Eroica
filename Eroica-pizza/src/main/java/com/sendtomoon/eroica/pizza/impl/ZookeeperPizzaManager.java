package com.sendtomoon.eroica.pizza.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

import com.sendtomoon.eroica.common.utils.PURL;
import com.sendtomoon.eroica.pizza.PizzaListener;
import com.sendtomoon.eroica.pizza.PizzaPathListener;

public class ZookeeperPizzaManager extends AbstractPizzaMananger {

	private static final int DEFAULT_PORT = 2181;

	private static final int ZK_JUTE_DEF_MAXBUFFER = 5 * 1024 * 1024;

	static {
		String value = System.getProperty("jute.maxbuffer");
		if (value == null || (value = value.trim()).length() == 0) {
			System.setProperty("jute.maxbuffer", String.valueOf(ZK_JUTE_DEF_MAXBUFFER));
		}
	}

	private ConcurrentMap<String, Object> _listeners;

	private ZkClient client;

	public ZookeeperPizzaManager() {
		
	}

	@Override
	protected void doInit(PURL configURL) {
		_listeners = new ConcurrentHashMap<String, Object>();
		String host = configURL.getHost();
		if (host == null || (host = host.trim()).length() == 0) {
			throw new IllegalArgumentException("Pizza manager<" + configURL + "> zookeeper host requried.");
		}
		int sessionTimeout = configURL.getParameter("sessionTimeout", 30000);
		int connectionTimeout = configURL.getParameter("connectionTimeout", Integer.MAX_VALUE);
		client = new ZkClient(configURL.getBackupAddress(DEFAULT_PORT), sessionTimeout, connectionTimeout);
	}

	@Override
	protected boolean doCreatePath(String path, boolean ephemeral) {
		try {
			client.create(path, null, ephemeral ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
		} catch (ZkNodeExistsException e) {
			return false;
		} catch (ZkNoNodeException e) {
			createParents(path);
			client.create(path, null, ephemeral ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
		}
		return true;
	}

	@Override
	protected boolean doAdd(String path, String content, boolean ephemeral) {
		try {
			client.create(path, content, ephemeral ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
		} catch (ZkNodeExistsException e) {
			return false;
		} catch (ZkNoNodeException e) {
			createParents(path);
			client.create(path, content, ephemeral ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
		}
		return true;
	}

	@Override
	protected boolean doDel(String path) {
		return client.delete(path);
	}

	@Override
	protected String doGet(String path) {
		String value = null;
		try {
			value = client.readData(path);
		} catch (ZkNoNodeException e) {
			logger.error(e, e);
			return null;
		}
		return value;
	}

	@Override
	protected boolean doRemovePathListener(String parentPath) {
		return doRemoveListener(parentPath);
	}

	@Override
	protected boolean doRemoveListener(String path) {
		synchronized (_listeners) {
			Object listener = _listeners.remove(path);
			if (listener != null) {
				// logger.warn("Unsubscribe listener for <"+path+">.");
				if (listener instanceof PizzaZkDataListener) {
					client.unsubscribeDataChanges(path, (PizzaZkDataListener) listener);
				} else {
					client.unsubscribeChildChanges(path, (PizzaZkPathListener) listener);
				}
				return true;
			}
			;
			return false;
		}
	}

	@Override
	protected List<String> doListChildren(String parentPath) {
		if (!client.exists(parentPath)) {
			return null;
		}
		return client.getChildren(parentPath);
	}

	@Override
	public int doCountChildren(String parentPath) {
		return client.countChildren(parentPath);
	}

	@Override
	protected void doSet(String path, String content, boolean ephemeral) {
		if (client.exists(path)) {
			client.writeData(path, content);
		} else {
			try {
				client.create(path, content, ephemeral ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
			} catch (ZkNodeExistsException e) {
				client.writeData(path, content);
			} catch (ZkNoNodeException e) {
				createParents(path);
				client.create(path, content, ephemeral ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
			}
		}
	}

	@Override
	public void doSetListener(String path, PizzaListener listener) {
		Object old = _listeners.get(path);
		if (old != null) {
			// logger.warn("Unsubscribe data changed listener for <"+path+">.");
			client.unsubscribeDataChanges(path, (PizzaZkDataListener) old);
		}
		if (listener != null) {
			PizzaZkDataListener zl = new PizzaZkDataListener(listener);
			client.subscribeDataChanges(path, zl);
			_listeners.put(path, zl);
		}
	}

	protected void doSetPathListener(String path, PizzaPathListener listener) {
		Object old = _listeners.get(path);
		if (old != null) {
			this.doRemoveListener(path);
		}
		if (listener != null) {
			PizzaZkPathListener zl = new PizzaZkPathListener(listener);
			client.subscribeChildChanges(path, zl);
			_listeners.put(path, zl);
		}
	}

	@Override
	protected boolean doExists(String path) {
		return client.exists(path);
	}

	@Override
	protected void doShutdown() {
		if (_listeners != null && _listeners.size() > 0) {
			_listeners.clear();
		}
		if (client != null) {
			ZkClient client = this.client;
			this.client = null;
			client.close();
		}
	}

	protected void createParents(String path) {
		int index = path.lastIndexOf('/');
		String parentPath = null;
		if (index > 0) {
			parentPath = path.substring(0, index);
			try {
				client.create(parentPath, null, CreateMode.PERSISTENT);
			} catch (ZkNodeExistsException e) {
				// 返回
			} catch (ZkNoNodeException e) {
				createParents(parentPath);
				client.create(parentPath, null, CreateMode.PERSISTENT);
			}
		}
	}

	public ZkClient getClient() {
		return client;
	}

	@Override
	public String toString() {
		return client == null ? this.toString() : client.toString();
	}

}
