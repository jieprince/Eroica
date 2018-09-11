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
package com.alibaba.dubbo.common.serialize.support.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

import com.alibaba.dubbo.common.utils.ClassHelper;

/**
 * Compacted java object input stream.
 * 
 * @author qianlei
 */

public class CompactedObjectInputStream extends ObjectInputStream {
	private ClassLoader mClassLoader;

	public CompactedObjectInputStream(InputStream in) throws IOException {
		this(in, Thread.currentThread().getContextClassLoader());
	}

	public CompactedObjectInputStream(InputStream in, ClassLoader cl)
			throws IOException {
		super(in);

		mClassLoader = cl == null ? ClassHelper.getClassLoader() : cl;
	}

	protected Class<?> resolveClass(ObjectStreamClass objectStreamClass)
			throws IOException, ClassNotFoundException {
		Class<?> clazz = Class.forName(objectStreamClass.getName(), false,
				this.mClassLoader);

		if (clazz != null) {
			return clazz;
		}
		return super.resolveClass(objectStreamClass);
	}

	protected Class<?> resolveProxyClass(String[] interfaces)
			throws IOException, ClassNotFoundException {
		Class<?>[] interfaceClasses = new Class[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			interfaceClasses[i] = Class.forName(interfaces[i], false,
					this.mClassLoader);
		}
		try {
			return Proxy.getProxyClass(this.mClassLoader, interfaceClasses);
		} catch (IllegalArgumentException e) {
		}
		return super.resolveProxyClass(interfaces);
	}
}
