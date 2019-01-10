package com.sendtomoon.eroica.fling.monitor.services;

import com.sendtomoon.eroica.fling.monitor.dtos.InstanceStatusDTO;

public interface IMonitorCollectService {

	int getAvailableProcessors();

	InstanceStatusDTO getInstanceStatus();

	int getAvailableMemory();
}
