package com.sendtomoon.eroica.appclient.dubbo;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.alibaba.dubbo.rpc.RpcContext;
import com.sendtomoon.eroica.common.appclient.IServiceClient;
import com.sendtomoon.eroica.common.appclient.ServiceParams;

public class DubboAsyncHandler {
	
	

	public static void asyncInvoke(final IServiceClient hellowordHandler, final ServiceParams serviceParams){
		RpcContext context = RpcContext.getContext();
		context.asyncCall(new Runnable() {
			@Override
			public void run() {
				hellowordHandler.invoke(serviceParams);
			}
		});
	}

	public static DubboAsyncFuture concurrentInvoke(Callable<Object> callable){
		RpcContext context=RpcContext.getContext();
		Future<Object> localFuture=context.asyncCall(callable);
		Future<Object> future=context.getFuture();
		if(future==null) {
			future=localFuture;
		}
		context.setFuture(null);
		return new DubboAsyncFuture(future);
	}
	
	
	public static DubboAsyncFuture concurrentInvoke(final IServiceClient serviceClient, final ServiceParams params){
		return concurrentInvoke(new Callable<Object>(){
			@Override
			public Object call() throws Exception {
				return serviceClient.invoke(params);
			}
		});
	}
	
}
