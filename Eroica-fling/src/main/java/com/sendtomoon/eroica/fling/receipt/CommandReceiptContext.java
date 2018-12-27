package com.sendtomoon.eroica.fling.receipt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.sendtomoon.eroica.fling.FlingCommand;
import com.sendtomoon.eroica.fling.FlingCommandReceiptor;
import com.sendtomoon.eroica.fling.msg.FlingCommandMsg;
import com.sendtomoon.eroica.fling.msg.FlingReceiptMsg;
import com.sendtomoon.eroica.pizza.PizzaListener;
import com.sendtomoon.eroica.pizza.PizzaManager;
import com.sendtomoon.eroica.pizza.PizzaPathListener;

public class CommandReceiptContext implements PizzaListener, PizzaPathListener {

	private static final long EXPIRED_TIME = 5 * 60 * 1000;

	private FlingCommandReceiptor receiptorHandler;

	protected static Log logger = LogFactory.getLog(CommandReceiptContext.class);

	private String pappName;

	private Long timestamp;

	private PizzaManager pizzaManager;

	private String rid;

	private Map<String, String> receiptInstances = new ConcurrentHashMap<String, String>();

	public CommandReceiptContext(PizzaManager pizzaManager, FlingCommandReceiptor receiptorHandler, String pappName,
			Long createTimestamp, String rid) {
		this.pappName = pappName;
		this.timestamp = createTimestamp;
		this.rid = rid;
		this.pizzaManager = pizzaManager;
		this.receiptorHandler = receiptorHandler;
		//
		pizzaManager.set(FlingCommand.PIZZA_GROUP_FLING_RECEIPT_NAME + "/" + rid, "createdDate=" + createTimestamp,
				true);
		pizzaManager.setListener(FlingCommand.PIZZA_GROUP_FLING_RECEIPT_NAME + "/" + rid, this);
		pizzaManager.setPathListener(FlingCommand.PIZZA_GROUP_FLING_RECEIPT2 + "/" + rid, this);
	}

	public String getPappName() {
		return pappName;
	}

	public void setPappName(String pappName) {
		this.pappName = pappName;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public FlingCommandReceiptor getReceiptorHandler() {
		return receiptorHandler;
	}

	public void setReceiptorHandler(FlingCommandReceiptor receiptorHandler) {
		this.receiptorHandler = receiptorHandler;
	}

	@Override
	public synchronized void pizzaPathChanged(String parentPath, List<String> childrenPaths) {
		if (childrenPaths != null && childrenPaths.size() > 0) {
			for (String ip : childrenPaths) {
				if (!receiptInstances.containsKey(ip)) {
					String pizzaContent = pizzaManager
							.get(FlingCommand.PIZZA_GROUP_FLING_RECEIPT2 + "/" + rid + "/" + ip);
					handleConfigChange(pizzaContent);
				}
			}
		}
	}

	public boolean isExpired() {
		Long timestamp = this.timestamp;
		return timestamp != null && (System.currentTimeMillis() > (timestamp + EXPIRED_TIME));
	}

	public synchronized void clear() {
		clear(this.rid, this.pappName, this.pizzaManager);
	}

	public static synchronized void clear(String cmdRid, String pappName, PizzaManager pizzaManager) {
		if (logger.isInfoEnabled()) {
			logger.info("clear command<" + cmdRid + "> for papp:" + pappName);
		}
		pizzaManager.removeListener(FlingCommand.PIZZA_GROUP_FLING_RECEIPT_NAME + "/" + cmdRid);
		//
		pizzaManager.del(FlingCommand.PIZZA_GROUP_FLING_RECEIPT_NAME + "/" + cmdRid);
		//
		pizzaManager.removeListener(FlingCommand.PIZZA_GROUP_FLING_RECEIPT2 + "/" + cmdRid);
		pizzaManager.forceDel(FlingCommand.PIZZA_GROUP_FLING_RECEIPT2 + "/" + cmdRid);
		//
		String cmdString = pizzaManager.get(FlingCommand.PIZZA_GROUP_FLING_NAME + "/" + pappName);
		if (cmdString != null && cmdString.length() > 0) {
			FlingCommandMsg cmd = JSONObject.parseObject(cmdString, FlingCommandMsg.class);
			if (cmdRid.equals(cmd.getRid())) {
				// 清空指令内容
				pizzaManager.set(FlingCommand.PIZZA_GROUP_FLING_NAME + "/" + pappName, "", true);
			}
		}
	}

	@Override
	public synchronized void handleConfigChange(String pizzaContent) {
		if (pizzaContent == null || pizzaContent.length() < 8) {
			return;
		}
		try {
			FlingReceiptMsg receiptMsg = JSONObject.parseObject(pizzaContent, FlingReceiptMsg.class);
			String ip = receiptMsg.getInstanceIp();
			if (receiptInstances.containsKey(ip)) {
				return;
			} else {
				receiptInstances.put(ip, ip);
			}
			if (logger.isInfoEnabled()) {
				logger.info("Command receipt=" + JSONObject.toJSONString(receiptMsg));
			}
			//
			receiptorHandler.receipt(receiptMsg);
		} catch (Throwable th) {
			logger.error("Handle Receipt error,\n" + pizzaContent + "\n,cause:" + th.getMessage());
		}
	}

}
