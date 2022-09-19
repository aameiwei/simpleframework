package org.simpleframework.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AspectV1 {
    //String pointcut();
    //"execution(* com.imooc.controller.frontend..*.*(..))"以及within(com.imooc.controller.frontend.*)
    /**
     * 版本一：需要被织入横切逻辑的注解标签
     */
    Class<? extends Annotation> value();
}
