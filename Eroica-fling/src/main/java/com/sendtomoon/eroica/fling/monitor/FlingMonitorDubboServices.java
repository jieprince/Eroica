package com.sendtomoon.eroica.fling.monitor;

import com.sendtomoon.eroica.fling.monitor.dtos.MonitorMsgDTO;

public interface FlingMonitorDubboServices {

	void sendMonitorMsg(MonitorMsgDTO msg);
}
