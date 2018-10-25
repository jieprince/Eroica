package com.sendtomoon.eroica.eoapp.protocol.dubbo;

import java.util.List;

import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;

public interface IDubboProtocol {

	List<RegistryConfig> getRegistryConfig(String[] registryIds);
	
	List<ProtocolConfig> getProtocolConfig(String[] protocolIds);
	
}
