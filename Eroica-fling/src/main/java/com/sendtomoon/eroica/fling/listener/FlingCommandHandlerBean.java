package com.sendtomoon.eroica.fling.listener;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
//import org.unidal.cat.Cat;
//import org.unidal.cat.message.Transaction;

import com.alibaba.fastjson.JSONObject;
import com.sendtomoon.eroica.common.utils.InstanceSystemPropertyUtils;
import com.sendtomoon.eroica.common.utils.MDCUtil;
import com.sendtomoon.eroica.common.utils.PNetUtils;
import com.sendtomoon.eroica.eoapp.esa.ResponseModel;
import com.sendtomoon.eroica.pizza.PizzaContext;
import com.sendtomoon.eroica.pizza.PizzaManager;
import com.sendtomoon.eroica.eoapp.EoAppContext;
import com.sendtomoon.eroica.eoapp.sar.SARContext;
import com.sendtomoon.eroica.fling.FlingCommand;
import com.sendtomoon.eroica.fling.msg.FlingCommandMsg;
import com.sendtomoon.eroica.fling.msg.FlingReceiptMsg;
import com.sendtomoon.eroica.fling.receipt.CommandReceiptPush;

public class FlingCommandHandlerBean implements FlingCommandHandler, InitializingBean {

	protected Log logger = LogFactory.getLog(this.getClass());

	private EoAppContext eoappContext;

	private PizzaManager pizzaManager;

	private String pizzaManagerURL;

	private PizzaContext pizzaContext;

	private static final long VALID_TIME = 5 * 60 * 1000;

	private volatile String lastRid;

	private String instanceIp;

	private int receiptVersion = 1;

	private CommandReceiptPush receiptPush;

	public FlingCommandHandlerBean() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pizzaManagerURL != null) {
			pizzaManager = pizzaContext.createManager(pizzaManagerURL);
		} else {
			pizzaManager = pizzaContext.getDefaultManager();
		}
		String instanceIp = InstanceSystemPropertyUtils.getInstanceIP();
		if (instanceIp == null) {
			instanceIp = PNetUtils.getLocalHost();
		}
		this.instanceIp = instanceIp;
	}

	protected boolean check(FlingCommandMsg cmd) {
		String pappName = pizzaContext.getAppName();
		if (!pappName.equals(cmd.getEoappName())) {
			logger.error("Command PApp<" + cmd.getEoappName() + "> not equals by current PApp<" + pappName + ">.");
			return false;
		}
		if (!checkLastRid(cmd)) {
			return false;
		}
		if (!checkTimestamp(cmd)) {
			return false;
		}
		// --------------------------------------------------
		if (instanceIp != null && !checkInstanceIp(cmd, instanceIp)) {
			return false;
		}
		return true;
	}

	protected boolean checkLastRid(FlingCommandMsg cmd) {
		if (this.lastRid != null && this.lastRid.equals(cmd.getRid())) {
			logger.error("Command:" + lastRid + " for PApp<" + cmd.getEoappName() + ">repeated.");
			return false;
		}
		this.lastRid = cmd.getRid();
		return true;
	}

	protected boolean checkInstanceIp(FlingCommandMsg cmd, String instanceIp) {
		List<String> limitIps = cmd.getLimitIps();
		// -------------------------------------------
		if (limitIps != null && limitIps.size() > 0) {
			boolean matched = false;
			for (String limitIp : limitIps) {
				if (instanceIp.equals(limitIp)) {
					matched = true;
					if (logger.isInfoEnabled()) {
						logger.info("Local instance ip:" + instanceIp + " matched for Command:" + cmd.getRid() + ".");
					}
					break;
				}
			}
			if (!matched) {
				logger.warn("Local instance ip=" + instanceIp + " not matched for Command=" + cmd.getRid()
						+ " by limitIPs=" + limitIps);
				return false;
			}
		}
		return true;
	}

	protected boolean checkTimestamp(FlingCommandMsg cmd) {
		long timestamp = cmd.getTimestamp();
		if (timestamp > 0) {
			long local = System.currentTimeMillis();
			if (timestamp < (local - VALID_TIME) || timestamp > (local + VALID_TIME)) {
				DateFormat df = DateFormat.getDateInstance();
				logger.error("Command for PApp<" + cmd.getEoappName() + "> timestamp erorr, command time="
						+ df.format(new Date(timestamp)) + ", local time=" + df.format(new Date(local)));

				return false;
			}
		}
		return true;
	}

	@Override
	public void handleCommand(FlingCommandMsg command) {
		try {
			innerHandleCommand(command);
		} catch (Throwable th) {
			logger.error("Handle command<" + command.getRid() + "> error,cause:" + th.getMessage(), th);
		}
	}

	public void innerHandleCommand(FlingCommandMsg command) {
		// --------------------------------------------
		if (logger.isInfoEnabled()) {
			logger.info("Handle Command:" + command.getRid() + ", and content=" + JSONObject.toJSONString(command));
		}

		if (!check(command)) {
			return;
		}
		try {
			ResponseModel result = null;
			long t1 = System.currentTimeMillis();
			try {
				if (FlingCommandMsg.TARGET_TYPE_EOAPP.equals(command.getTargetType())) {
					result = handleForPApp(command);
				} else if (FlingCommandMsg.TARGET_TYPE_SAR.equals(command.getTargetType())) {
					result = handleForSAR(command);
				} else {
					result = new ResponseModel(FlingReceiptMsg.RCODE_FATAL_ERROR,
							"Error targetType=" + command.getTargetType());
				}
			} catch (Throwable th) {
				logger.error("Handle command<" + command.getRid() + "> error,cause:" + th.getMessage(), th);
				result = new ResponseModel(FlingReceiptMsg.RCODE_FATAL_ERROR, th.getMessage());
			}
			long elapsedTime = System.currentTimeMillis() - t1;
			sendReceiptMsg(command, result, elapsedTime);
		} finally {
		}
	}

	protected ResponseModel handleForSAR(FlingCommandMsg command) {
		try {
			String sarName = command.getSarName();
			if (sarName == null || (sarName = sarName.trim()).length() == 0) {
				logger.error("sarName required");
				return new ResponseModel(FlingReceiptMsg.RCODE_FATAL_ERROR, "sarName required");
			}
			if (!eoappContext.isRunning()) {
				return new ResponseModel(FlingReceiptMsg.RCODE_FATAL_ERROR, "PApp shutdowned.");
			}
			if (FlingCommandMsg.ACTION_TYPE_STARTUP.equals(command.getActionType())) {
				if (eoappContext.isRunning(sarName) || eoappContext.startup(sarName)) {
					return new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					return new ResponseModel(FlingReceiptMsg.RCODE_SAR_CMD_FAILURE, null);
				}
			} else if (FlingCommandMsg.ACTION_TYPE_SHUTDOWN.equals(command.getActionType())) {
				if (eoappContext.shutdown(sarName)) {
					return new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					return new ResponseModel(FlingReceiptMsg.RCODE_SAR_CMD_FAILURE, null);
				}
			} else if (FlingCommandMsg.ACTION_TYPE_RESTARTUP.equals(command.getActionType())) {
				try {
					eoappContext.shutdown(sarName);
				} catch (Throwable th) {

				}
				if (eoappContext.startup(sarName)) {
					return new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					return new ResponseModel(FlingReceiptMsg.RCODE_SAR_CMD_FAILURE, null);
				}
			} else if (FlingCommandMsg.ACTION_TYPE_UNEXPORT_ESA.equals(command.getActionType())) {
				if (eoappContext.unexport(sarName)) {
					return new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					return new ResponseModel(FlingReceiptMsg.RCODE_SAR_CMD_FAILURE, null);
				}
			} else if (FlingCommandMsg.ACTION_TYPE_ECHO.equals(command.getActionType())) {
				SARContext sar = eoappContext.getSARContext(sarName, false);
				if (sar == null) {
					return new ResponseModel(FlingReceiptMsg.RCODE_SAR_CMD_FAILURE, "NotExists.");
				} else if (sar.isRunning()) {
					return new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					return new ResponseModel(FlingReceiptMsg.RCODE_SAR_CMD_FAILURE, "shutdowned.");
				}
			} else {
				return new ResponseModel(FlingReceiptMsg.RCODE_FATAL_ERROR,
						"Error actionType=" + command.getActionType());
			}
		} finally {
		}
	}

	protected ResponseModel handleForPApp(FlingCommandMsg command) throws Exception {
		try {
			ResponseModel model = null;
			if (FlingCommandMsg.ACTION_TYPE_STARTUP.equals(command.getActionType())) {
				if (eoappContext.isRunning() || eoappContext.startup()) {
					model = new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					model = new ResponseModel(FlingReceiptMsg.RCODE_APP_CMD_FAILURE, "PApp startuped.");
				}
			} else if (FlingCommandMsg.ACTION_TYPE_SHUTDOWN.equals(command.getActionType())) {
				if (eoappContext.shutdown()) {
					model = new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					model = new ResponseModel(FlingReceiptMsg.RCODE_APP_CMD_FAILURE, "PApp shutdowned.");
				}
			} else if (FlingCommandMsg.ACTION_TYPE_RESTARTUP.equals(command.getActionType())) {
				try {
					eoappContext.shutdown();
				} catch (Throwable th) {
				}
				if (eoappContext.startup()) {
					model = new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					model = new ResponseModel(FlingReceiptMsg.RCODE_APP_CMD_FAILURE, "PApp startuped.");
				}
			} else if (FlingCommandMsg.ACTION_TYPE_UNEXPORT_ESA.equals(command.getActionType())) {
				if (eoappContext.unexportAll()) {
					model = new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					model = new ResponseModel(FlingReceiptMsg.RCODE_APP_CMD_FAILURE, null);
				}
			} else if (FlingCommandMsg.ACTION_TYPE_ECHO.equals(command.getActionType())) {
				boolean isRunning = eoappContext.isRunning();
				if (isRunning) {
					model = new ResponseModel(FlingReceiptMsg.RCODE_SUCCESS, null);
				} else {
					model = new ResponseModel(FlingReceiptMsg.RCODE_APP_CMD_FAILURE, "Shutdonwed.");
				}
			} else {
				model = new ResponseModel(FlingReceiptMsg.RCODE_FATAL_ERROR,
						"Error actionType=" + command.getActionType());
			}
			return model;
		} finally {
		}
	}

	public void sendReceiptMsg(FlingCommandMsg command, ResponseModel result, long elapsedTime) {
		if (receiptVersion < 1) {
			if (logger.isInfoEnabled()) {
				logger.info("Disabled post receipt,cause receiptVersion=" + receiptVersion);
			}
			return;
		}
		FlingReceiptMsg msg = new FlingReceiptMsg();
		msg.setActionType(command.getActionType());
		msg.setProjectId(command.getProjectId());
		msg.setDomainId(command.getDomainId());
		msg.setPappName(command.getEoappName());
		msg.setReceiptDate(System.currentTimeMillis());
		msg.setInstanceIp(instanceIp);
		msg.setInstanceName(InstanceSystemPropertyUtils.getInstanceName());
		msg.setRid(command.getRid());
		msg.setSarName(command.getSarName());
		msg.setTargetType(command.getTargetType());
		msg.setResponseCode(result.getResponseCode());
		msg.setResponseMsg(result.getResponseMsg());
		msg.setLogId(MDCUtil.peek().getRequestId());
		msg.setElapsedTime(elapsedTime);
		String msgString = JSONObject.toJSONString(msg);

		try {
			//
			if (receiptVersion == 2) {
				String pizzaPath = FlingCommand.PIZZA_GROUP_FLING_RECEIPT2 + "/" + msg.getRid() + "/" + instanceIp;
				pizzaManager.set(pizzaPath, msgString, true);
			} else {
				// ----------------------------------------------------------------------------------------------------
				int ipInt = PNetUtils.getLocalAddress().hashCode();
				// 防止时间冲突
				int wait = Math.abs(ipInt) % 16 * 150;
				if (logger.isDebugEnabled()) {
					logger.debug("InstanceIp=" + instanceIp + ",intValue=" + ipInt + ",Receipt waitTime=" + wait);
				}
				Thread.sleep(wait);
				String pizzaPath = FlingCommand.PIZZA_GROUP_FLING_RECEIPT_NAME + "/" + msg.getRid();
				pizzaManager.set(pizzaPath, msgString, true);
			}
			if (logger.isInfoEnabled()) {
				logger.info("Post receipt for Command:" + command.getRid() + ",result:" + msgString);
			}

		} catch (Throwable e) {
			if (logger.isErrorEnabled()) {
				logger.error("Post Receipt error for command<" + msgString + ">,cause:" + e.getMessage(), e);
			}
		}
	}

	public EoAppContext getPappContext() {
		return eoappContext;
	}

	public void setPappContext(EoAppContext pappContext) {
		this.eoappContext = pappContext;
	}

	public int getReceiptVersion() {
		return receiptVersion;
	}

	public void setReceiptVersion(int receiptVersion) {
		this.receiptVersion = receiptVersion;
	}

	public String getPizzaManagerURL() {
		return pizzaManagerURL;
	}

	public void setPizzaManagerURL(String pizzaManagerURL) {
		this.pizzaManagerURL = pizzaManagerURL;
	}

	public PizzaContext getPizzaContext() {
		return pizzaContext;
	}

	public void setPizzaContext(PizzaContext pizzaContext) {
		this.pizzaContext = pizzaContext;
	}

	public CommandReceiptPush getReceiptPush() {
		return receiptPush;
	}

	public void setReceiptPush(CommandReceiptPush receiptPush) {
		this.receiptPush = receiptPush;
	}

}
