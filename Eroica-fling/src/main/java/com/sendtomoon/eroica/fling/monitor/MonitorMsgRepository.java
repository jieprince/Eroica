package com.sendtomoon.eroica.fling.monitor;

import com.sendtomoon.eroica.fling.monitor.dtos.MonitorMsgDTO;

public interface MonitorMsgRepository {

	void onMessage(MonitorMsgDTO msg) throws Exception;

}
