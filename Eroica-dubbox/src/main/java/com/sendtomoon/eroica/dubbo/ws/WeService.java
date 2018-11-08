/*
 * Copyright 1999-2012 Alibaba Group.
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
package com.sendtomoon.eroica.dubbo.ws;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service
 * 
 * @author william.liangf
 * @export
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface WeService {

    String version() default "";

    String group() default "";

    String id() default "";

    boolean export() default false;

    String token() default "";

    boolean deprecated() default false;

    boolean dynamic() default false;

    String accesslog() default "";

    int executes() default 0;

    boolean register() default false;

    int weight() default 0;

    String document() default "";

    int delay() default 0;

    String mock() default "";

    String cluster() default "";

    String proxy() default "";

    int connections() default 0;

    int callbacks() default 0;

    //String onconnect() default "";

  //  String ondisconnect() default "";

    String owner() default "";

    String layer() default "";

    int retries() default 0;

    String loadbalance() default "";

    boolean async() default false;

    int actives() default 0;

    boolean sent() default false;

    int timeout() default 0;

    String[] filter() default {};

    //String[] listener() default {};

    //String[] parameters() default {};

    //String application() default "";

    //String module() default "";

    //String provider() default "";

    String[] protocol() default {};

    String monitor() default "";

    String[] registry() default {};
    
    String scope() default "";

}
