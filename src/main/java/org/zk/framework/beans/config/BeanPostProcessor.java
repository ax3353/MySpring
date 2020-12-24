package org.zk.framework.beans.config;

public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }


    default Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }
}