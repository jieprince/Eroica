package com.sendtomoon.eroica.fling.listener;

import com.sendtomoon.eroica.fling.msg.FlingCommandMsg;

public interface FlingCommandHandler {
	
	void handleCommand(FlingCommandMsg command);
	
}
