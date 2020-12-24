package org.zk.framework.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BeanDefinition {

    /**
     * 扫描到的Bean类的名字
     */
    private String BeanClassName;

    /**
     * IoC容器中的key
     */
    private String FactoryBeanName;

    /**
     * 是否延迟加载
     */
    private boolean lazyInit = false;

    public BeanDefinition(String factoryBeanName, String beanClassName) {
        FactoryBeanName = factoryBeanName;
        BeanClassName = beanClassName;
    }
}
