package com.sendtomoon.eroica.fling;

import com.sendtomoon.eroica.fling.msg.FlingCommandMsg;

public interface FlingCommand {

	public static final String PIZZA_GROUP_FLING_NAME = "fling";

	public static final String PIZZA_GROUP_FLING_RECEIPT_NAME = "fling_receipt";

	public static final String PIZZA_GROUP_FLING_RECEIPT2 = "fling_receipt2";

	/***
	 * 发送指令
	 * 
	 * @param cmd 指令内容
	 * @return 指令流水ID
	 */
	String send(FlingCommandMsg command);

	void clearExpiredListeners();

	void clear(String rid, String pappName);

}
