package org.zk.framework.context.support;

import org.zk.framework.beans.config.BeanPostProcessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractApplicationContext {

    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    protected void refresh() throws Exception {
    }
}
