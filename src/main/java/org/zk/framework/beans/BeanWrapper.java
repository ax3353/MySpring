package org.zk.framework.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述: BeanWrapper对原生对象的包装，放入factoryBeanInstanceCache的只是BeanWrapper
 * @author kun.zhu
 * @date 2020/12/21 16:52
 */
@Data
@NoArgsConstructor
public class BeanWrapper {

    /**
     * 被包装的实例
     */
    private Object wrappedBean;

    /**
     * 被包装的class
     */
    private Class<?> wrappedClass;

    public BeanWrapper(Object wrappedBean) {
        this.wrappedBean = wrappedBean;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedBean.getClass();
    }
}
