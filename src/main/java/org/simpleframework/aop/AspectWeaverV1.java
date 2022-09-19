package org.simpleframework.aop;

import org.simpleframework.aop.annotation.AspectV1;
import org.simpleframework.aop.annotation.Order;
import org.simpleframework.aop.aspect.AspectInfoV1;
import org.simpleframework.aop.aspect.DefaultAspect;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.util.*;

public class AspectWeaverV1 {
    private BeanContainer beanContainer;

    public AspectWeaverV1() {
        this.beanContainer = BeanContainer.getInstance();
    }

    public void doAop() {
        //1.获取所有的切面类
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(AspectV1.class);
        //2.将切面类按照不同的织入目标进行切分
        Map<Class<? extends Annotation>,List<AspectInfoV1>> categorizedMap = new HashMap<>();
        if(ValidationUtil.isEmpty(aspectSet)){return;}
        for (Class<?> aspectClass : aspectSet) {
            //验证合法性
            if (verifyAspect(aspectClass)) {
                categorizeAspect(categorizedMap,aspectClass);
            }else {
                throw new RuntimeException("@Aspect and @Order must be added to the Aspect class, and Aspect class must extend from DefaultAspect");
            }
        }
        //3.按照不同的织入目标分别去植入Aspect逻辑
        if(ValidationUtil.isEmpty(categorizedMap)){return;}
        for (Class<? extends Annotation> category : categorizedMap.keySet()) {
            weaveByCategory(category,categorizedMap.get(category));
        }

    }

    //将切面类按照不同的织入目标进行切分
    private void categorizeAspect(Map<Class<? extends Annotation>, List<AspectInfoV1>> categorizedMap, Class<?> aspectClass) {
        Order orderTag = aspectClass.getAnnotation(Order.class);
        AspectV1 aspectTag = aspectClass.getAnnotation(AspectV1.class);
        DefaultAspect defaultAspect = (DefaultAspect) beanContainer.getBean(aspectClass);
        AspectInfoV1 aspectInfo = new AspectInfoV1(orderTag.value(), defaultAspect);
        if (!categorizedMap.containsKey(aspectTag.value())){
            //如果织入的joinPoint第一次出现，则以该joinPoint为key，以新创建的List<AspectInfoV1>为value
             List<AspectInfoV1> aspectInfoList = new ArrayList<>();
             aspectInfoList.add(aspectInfo);
             categorizedMap.put(aspectTag.value(),aspectInfoList);
        }else {
            //如果织入的joinPoint不是第一次出现，则以该joinPoint对应的value里添加新的Aspect逻辑
            List<AspectInfoV1> aspectInfoList = categorizedMap.get(aspectTag.value());
            aspectInfoList.add(aspectInfo);
        }
    }

    private void weaveByCategory(Class<? extends Annotation> category, List<AspectInfoV1> aspectInfoList) {
        //1.获取被代理类的集合
        Set<Class<?>> classesSet = beanContainer.getClassesByAnnotation(category);
        if(ValidationUtil.isEmpty(classesSet)){return;}
        //2.遍历被代理类，分别为每个代理类生成动态代理实例
        for (Class<?> targetClass : classesSet) {
            //创建动态代理对象
            AspectListExecutorV1 aspectListExecutor = new AspectListExecutorV1(targetClass,aspectInfoList);
            Object proxyBean = ProxyCreator.createProxy(targetClass, aspectListExecutor);
            //3.将动态代理类实例添加到容器里，取代未被代理的类实例
            beanContainer.addBean(targetClass,proxyBean);
        }


    }


    //框架中一定要遵守给Aspect类添加@Aspect和@Order标签的规范，同时，必须继承自DefaultAspect.class
    //此外，@Aspect的属性值不能是它本身
    private boolean verifyAspect(Class<?> aspectClass) {
        return aspectClass.isAnnotationPresent(AspectV1.class) &&
                aspectClass.isAnnotationPresent(Order.class) &&
                DefaultAspect.class.isAssignableFrom(aspectClass);
    }
}
