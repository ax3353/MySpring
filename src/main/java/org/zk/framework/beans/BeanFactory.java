package org.zk.framework.beans;

public interface BeanFactory {

    /**
     * 描述: 根据beanName从IoC容器中获取bean
     * @author kun.zhu
     * @date 2020/12/21 14:32
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 描述: 根据bean Class从IoC容器中获取bean
     * @author kun.zhu
     * @date 2020/12/21 14:32
     */
    Object getBean(Class<?> beanClass) throws Exception;
}
