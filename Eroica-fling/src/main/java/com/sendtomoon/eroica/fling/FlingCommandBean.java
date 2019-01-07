package com.sendtomoon.eroica.fling;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSONObject;
import com.sendtomoon.eroica.fling.msg.FlingCommandMsg;
import com.sendtomoon.eroica.fling.receipt.CommandReceiptContext;
import com.sendtomoon.eroica.pizza.PizzaManager;

public class FlingCommandBean implements FlingCommand, DisposableBean, InitializingBean {

	protected Log logger = LogFactory.getLog(this.getClass());

	private FlingCommandReceiptor commandReceiptor;

	private boolean commandReceiptEnable = true;

	private ConcurrentHashMap<String, CommandReceiptContext> commandsMap;

	private PizzaManager pizzaManager;

	@Override
	public String send(FlingCommandMsg cmd) {
		String rid = cmd.getRid();
		if (rid == null) {
			rid = createRID();
			cmd.setRid(rid);
		}
		Long curTimestamp = System.currentTimeMillis();
		cmd.setTimestamp(curTimestamp);
		//
		String cmdString = JSONObject.toJSONString(cmd);
		if (logger.isInfoEnabled()) {
			logger.info("Send command=" + cmdString);
		}
		if (cmd.getPappName() == null) {
			throw new java.lang.IllegalArgumentException("pappName required.");
		}
		pizzaManager.set(FlingCommand.PIZZA_GROUP_FLING_NAME + "/" + cmd.getPappName(), cmdString, true);
		if (commandReceiptEnable && commandReceiptor != null) {
			// ---------------------------------------------
			commandsMap.put(rid, new CommandReceiptContext(pizzaManager, this.commandReceiptor, cmd.getPappName(),
					curTimestamp, rid));
		}
		return rid;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.isCommandReceiptEnable()) {
			commandsMap = new ConcurrentHashMap<String, CommandReceiptContext>();
		}
	}

	@Override
	public void clearExpiredListeners() {
		if (commandsMap != null) {
			Set<String> rids = commandsMap.keySet();
			for (String rid : rids) {
				CommandReceiptContext cmd = commandsMap.get(rid);
				if (cmd != null && cmd.isExpired() && (cmd = commandsMap.remove(rid)) != null) {
					try {
						cmd.clear();
					} catch (Throwable th) {
						logger.error(th.getMessage(), th);
					}
				}
			}
		}
	}

	@Override
	public void clear(String cmdRid, String pappName) {
		if (commandsMap != null) {
			commandsMap.remove(cmdRid);
		}
		CommandReceiptContext.clear(cmdRid, pappName, pizzaManager);

	}

	protected String createRID() {
		char[] ticket = UUID.randomUUID().toString().toCharArray();
		StringBuilder sb = new StringBuilder(ticket.length);
		for (int i = 0; i < ticket.length; i++) {
			if (ticket[i] != '-') {
				sb.append(ticket[i]);
			}
		}
		return "C" + sb;
	}

	@Override
	public void destroy() throws Exception {
		if (commandsMap != null) {
			Set<String> rids = commandsMap.keySet();
			for (String rid : rids) {
				try {
					CommandReceiptContext cmd = commandsMap.remove(rid);
					if (cmd != null) {
						cmd.clear();
					}
				} catch (Throwable th) {
					logger.error(th.getMessage(), th);
				}
			}
		}
	}

	public FlingCommandReceiptor getCommandReceiptor() {
		return commandReceiptor;
	}

	public void setCommandReceiptor(FlingCommandReceiptor commandReceiptor) {
		this.commandReceiptor = commandReceiptor;
	}

	public boolean isCommandReceiptEnable() {
		return commandReceiptEnable;
	}

	public void setCommandReceiptEnable(boolean commandReceiptEnable) {
		this.commandReceiptEnable = commandReceiptEnable;
	}

	public PizzaManager getPizzaManager() {
		return pizzaManager;
	}

	public void setPizzaManager(PizzaManager pizzaManager) {
		this.pizzaManager = pizzaManager;
	}

}
