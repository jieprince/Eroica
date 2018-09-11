/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * 记录Service的Access Log。
 * <p>
 * 使用的Logger key是<code><b>dubbo.accesslog</b></code>。
 * 如果想要配置Access Log只出现在指定的Appender中，可以在Log4j中注意配置上additivity。配置示例:
 * <code>
 * <pre>
 * &lt;logger name="<b>dubbo.accesslog</b>" <font color="red">additivity="false"</font>&gt;
 *    &lt;level value="info" /&gt;
 *    &lt;appender-ref ref="foo" /&gt;
 * &lt;/logger&gt;
 * </pre></code>
 * 
 * @author ding.lid
 */
@Activate(group = Constants.PROVIDER, value = Constants.ACCESS_LOG_KEY)
public class AccessLogFilter implements Filter {
    
    private static final Logger logger            = LoggerFactory.getLogger(AccessLogFilter.class);

    private static final Logger accessLogger            = LoggerFactory.getLogger("dubbo.AccessLog");

    
    protected void log(Invoker<?> invoker,Invocation inv,double times){
    	 try {
    		 URL invokerURL=invoker.getUrl();
             String accesslog = invokerURL.getParameter(Constants.ACCESS_LOG_KEY);
             if (ConfigUtils.isNotEmpty(accesslog) && ConfigUtils.isDefault(accesslog)) {
                 RpcContext context = RpcContext.getContext();
                 String serviceName = invokerURL.getParameter(Constants.INTERFACE_KEY);
                 String version = invokerURL.getParameter(Constants.VERSION_KEY);
                 String group = invokerURL.getParameter(Constants.GROUP_KEY);
                 StringBuilder sn = new StringBuilder();
                 if(context.getRemoteHost()==null){
                	 sn.append(" UNKOWN");
                 }else{
                	 sn.append(" ").append(context.getRemoteHost()).append(":").append(context.getRemotePort());
                 }
                 sn.append("->").append(context.getLocalHost()).append(":").append(context.getLocalPort())
                 .append(",service<");
                 if (null != group && group.length() > 0) {
                     sn.append(group).append("/");
                 }
                 sn.append(serviceName);
                 if (null != version && version.length() > 0) {
                     sn.append(":").append(version);
                 }
                 sn.append(">, application<");
                 //
                 String application = invokerURL.getParameter(Constants.APPLICATION_KEY);
                 sn.append(application);
                 sn.append(">, method<");
                 sn.append(inv.getMethodName());
                 sn.append(">,times<"+times+">ms");
               
               
                 /*sn.append("(");
                 Class<?>[] types = inv.getParameterTypes();
                 if (types != null && types.length > 0) {
                     boolean first = true;
                     for (Class<?> type : types) {
                         if (first) {
                             first = false;
                         } else {
                             sn.append(",");
                         }
                         sn.append(type.getName());
                     }
                 }
                 sn.append(") ");
                 Object[] args = inv.getArguments();
                 if (args != null && args.length > 0) {
                     sn.append(Arrays.toString(args));
                 }*/
                 accessLogger.info(sn.toString());
             }
         } catch (Throwable t) {
             logger.warn("Exception in AcessLogFilter of service(" + invoker + " -> " + inv + ")", t);
         }
    }

    public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException {
    	long t1=System.nanoTime();
    	try{
        	return invoker.invoke(inv);
        }finally{
        	log(invoker,inv,(System.nanoTime()-t1)/1000/1000.0);
        }
    }

}