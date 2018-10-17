package com.sendtomoon.eroica.eoapp.protocol.jetty;

import org.eclipse.jetty.server.handler.ContextHandler;

import com.sendtomoon.eroica.eoapp.web.support.ServletContextResolver;

public interface JettyProtocol extends ServletContextResolver{
	
	ContextHandler getContext();
}
