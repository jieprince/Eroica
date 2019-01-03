package com.sendtomoon.eroica.common.appclient;


public interface IServiceClient  {
	
	/**
	 * Action调用
	 * @param params 入参,map/dto对象,框架自动会转换为map对象
	 * @return 返回结果
	 */
	ServiceResults invoke(Object params);
	
	/***
	 * Action调用 
	 * @param  params 入参,map/dto对象,框架自动会转换为map对象
	 * @param resultBindClass 返回结果(map)自动绑定class
	 */
	<T> T invoke(Object params,Class<T> resultBindClass);
	
	/***
	 * Action调用 
	 * @param  params 入参,map/dto对象,框架自动会转换为map对象
	 * @param resultBindClass 返回结果(map)自动绑定class
	 * @param  resultKey 从返回结果(map)中指定子map进行绑定
	 */
	<T> T invoke(Object params,Class<T> resultBindClass,String resultKey);
	
	/**
	 * @deprecated
	 */
	ServiceResults invoke();
	/**
	 * @deprecated
	 */
	<T> T invoke(Class<T> resultBindClass);
	/**
	 * @deprecated
	 */
	<T> T invoke(Class<T> resultBindClass,String resultKey);
	
}
