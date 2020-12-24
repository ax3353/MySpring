package org.zk.framework.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zk.framework.annotation.Autowired;
import org.zk.framework.annotation.Controller;
import org.zk.framework.annotation.Service;
import org.zk.framework.beans.BeanDefinition;
import org.zk.framework.beans.BeanFactory;
import org.zk.framework.beans.BeanWrapper;
import org.zk.framework.beans.config.BeanPostProcessor;
import org.zk.framework.beans.support.BeanDefinitionReader;
import org.zk.framework.context.support.AbstractApplicationContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext extends AbstractApplicationContext implements BeanFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationContext.class);

    private final String[] configLocations;
    private BeanDefinitionReader reader;

    /**
     * 用来保存bean定义信息
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * 用来保证注册式单例的容器
     */
    private final Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    /**
     * 用来存储所有的被代理过的对象
     */
    private final Map<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;

        try {
            this.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void refresh() throws Exception {
        //1、读取配置文件
        reader = new BeanDefinitionReader(this.configLocations);

        //2、解析配置文件，将配置信息变成BeanDefinition对象
        List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3、把BeanDefinition对应的实例注册到beanDefinitionMap key=beanName, value=beanDefinition对象
        this.doRegisterBeanDefinition(beanDefinitions);

        //4、完成依赖注入，在调用getBean()的才注入，把不是延时加载的类，提前初始化
        this.doAutowired();
    }

    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String factoryBeanName = beanDefinition.getFactoryBeanName();
            if (beanDefinitionMap.containsKey(factoryBeanName)) {
                throw new Exception("The " + factoryBeanName + " is existed.");
            }
            beanDefinitionMap.put(factoryBeanName, beanDefinition);
        }
    }

    private void doAutowired() {
        beanDefinitionMap.forEach((k, v) -> this.getBean(k));
    }

    @Override
    public Object getBean(Class<?> beanClass) {
        return getBean(beanClass.getName());
    }

    @Override
    public Object getBean(String beanName) {
        try {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

            Object wrappedBean = null;
            // 前置处理
            wrappedBean = this.applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);

            // 实例化bean
            wrappedBean = this.instantiateBean(beanDefinition);

            // 包装bean
            BeanWrapper beanWrapper = new BeanWrapper(wrappedBean);
            this.factoryBeanInstanceCache.put(beanName, beanWrapper);

            // 后置处理
            wrappedBean = this.applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);

            // 填充bean
            this.populateBean(beanWrapper);
            return wrappedBean;
        } catch (Exception e) {
            LOG.error("Get bean error", e);
            throw new RuntimeException(e);
        }
    }

    private Object instantiateBean(BeanDefinition beanDefinition) throws Exception {
        String beanClassName = beanDefinition.getBeanClassName();

        if (this.factoryBeanObjectCache.containsKey(beanClassName)) {
            return this.factoryBeanObjectCache.get(beanClassName);
        }

        Object instance = Class.forName(beanClassName).newInstance();
        this.factoryBeanObjectCache.put(beanClassName, instance);
        return instance;
    }

    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws Exception {
        Object result = existingBean;
        for (BeanPostProcessor processor : this.getBeanPostProcessors()) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws Exception {
        Object result = existingBean;
        for (BeanPostProcessor processor : this.getBeanPostProcessors()) {
            Object current = processor.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    private void populateBean(BeanWrapper beanWrapper) throws Exception {
        Object wrappedBean = beanWrapper.getWrappedBean();
        Class<?> clz = beanWrapper.getWrappedClass();

        // 只有加了以下注解的类，才执行注入
        if (!clz.isAnnotationPresent(Controller.class) && !clz.isAnnotationPresent(Service.class)) {
            return;
        }

        Field[] declaredFields = clz.getDeclaredFields();
        for (Field field : declaredFields) {
            // 只有加了以下注解的字段才执行注入
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowiredName = autowired.value().trim();
            if ("".equals(autowiredName)) {
                autowiredName = field.getType().getName();
            }

            // 暴力访问
            field.setAccessible(true);

            // 从IoC容器中获取要注入的实例并设置到字段上
            field.set(wrappedBean, factoryBeanInstanceCache.get(autowiredName).getWrappedBean());
        }
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[0]);
    }
}
