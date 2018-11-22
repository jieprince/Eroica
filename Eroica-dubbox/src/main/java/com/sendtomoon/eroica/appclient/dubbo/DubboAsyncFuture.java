package com.sendtomoon.eroica.appclient.dubbo;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.alibaba.dubbo.rpc.RpcException;
import com.sendtomoon.eroica.ac.dubbo.GenericResult;
import com.sendtomoon.eroica.common.appclient.ServiceResults;


public class DubboAsyncFuture {
	
	

	
	 DubboAsyncFuture(Future<Object> future){
		this.future=future;
	}
	
	
	
	
	private Future<Object> future;
	
	public boolean isDone(){
		return future==null?false:future.isDone();
	}
	
	public boolean isCancelled(){
		return future==null?false:future.isCancelled();
	}
	
	public boolean cancel(boolean mayInterruptIfRunning){
		return future==null?false:future.cancel(mayInterruptIfRunning);
	}
	
	
	public ServiceResults  getResult(){
		Object result=null;
		try {
			if(future!=null){
				result =future.get();
			}
		} catch(RuntimeException ex){
			throw ex;
		} catch (Exception e) {
			throw new RpcException(e.getMessage(),e);
		}
		return toServiceResults(result);
	}
	
	public ServiceResults  getResultAllowTimeout(long timeout, TimeUnit unit){
		Object result=null;
		try {
			if(future!=null){
				result = future.get(timeout, unit);
			}
		}catch(RuntimeException ex){
			throw ex;
		}catch(TimeoutException ex){
			return null;
		}catch (Exception e) {
			throw new RpcException(e.getMessage(),e);
		}
		return toServiceResults(result);
	}
	
	public ServiceResults  getResult(long timeout, TimeUnit unit) throws  TimeoutException{
		Object result=null;
		try {
			if(future!=null){
				result =future.get(timeout, unit);
			}
		}catch(RuntimeException ex){
			throw ex;
		}catch(TimeoutException ex){
			throw ex;
		}catch (Exception e) {
			throw new RpcException(e.getMessage(),e);
		}
		return toServiceResults(result);
	}
	
	@SuppressWarnings("unchecked")
	protected ServiceResults  toServiceResults(Object result){
		if(result==null)
		{
			return null;
		}
		if(result instanceof ServiceResults) {
			return (ServiceResults)result;
		}else {
			GenericResult genericResult=(GenericResult)result;
			Map map=(genericResult==null?null:genericResult.getResult());
			if(map!=null){
				return new ServiceResults(map);
			}
			return null;
		}
		
	}
}
