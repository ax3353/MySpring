package org.zk.framework.beans.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zk.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 描述: Bean Definition Reader
 *
 * @author kun.zhu
 * @date 2020/12/19 16:21
 */
public class BeanDefinitionReader {
    private static final Logger LOG = LoggerFactory.getLogger(BeanDefinitionReader.class);

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String CLASS_SUFFIX = ".class";

    private final Properties contextConfig;
    private final List<String> registryBeanClasses;

    public BeanDefinitionReader(String... configLocations) {
        this.contextConfig = new Properties();
        this.registryBeanClasses = new ArrayList<>();

        //1、读取配置文件
        this.doLoadConfig(configLocations);

        //2、扫描相关的类
        this.doScanner(contextConfig.getProperty("basePackages"));
    }

    private void doScanner(String... basePackages) {
        for (String basePackage : basePackages) {
            URL url = this.getClass().getResource("/" + basePackage.trim().replaceAll("\\.", "/"));
            File file = new File(url.getFile());
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    this.doScanner(basePackage + "." + f.getName());
                } else {
                    if (!f.getName().endsWith(CLASS_SUFFIX)) {
                        continue;
                    }

                    String className = basePackage + "." + f.getName().replace(CLASS_SUFFIX, "");
                    registryBeanClasses.add(className);
                }
            }
        }
    }

    private void doLoadConfig(String... configLocation) {
        // TODO 不应该简单处理
        String firstConfigLocation = configLocation[0];
        InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream(firstConfigLocation.replaceFirst(CLASSPATH_PREFIX, ""));
        try {
            this.contextConfig.load(input);
        } catch (IOException e) {
            LOG.error("load config file error", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.error("load config file error", e);
                }
            }
        }
    }

    public List<BeanDefinition> loadBeanDefinitions() {
        List<BeanDefinition> definitions = new ArrayList<>();
        try {
            for (String beanClzName : registryBeanClasses) {
                Class<?> beanClass = Class.forName(beanClzName);
                if (beanClass.isInterface()) {
                    continue;
                }

                //beanName有三种情况:
                //1、默认是类名首字母小写
                definitions.add(this.doCreateBeanDefinition(beanClass.getSimpleName(), beanClass.getName()));

                //2、接口注入
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> clz : interfaces) {
                    // TODO 如果找到多个实现类则抛异常
                    //3、自定义名字
                    definitions.add(this.doCreateBeanDefinition(clz.getName(), beanClass.getName()));
                }
            }
        } catch (ClassNotFoundException e) {
            LOG.error("Load BeanDefinition Error", e);
        }
        return definitions;
    }

    /**
     * 描述: 构建BeanDefinition
     *
     * @author kun.zhu
     * @date 2020/12/21 10:49
     */
    private BeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        String factoryBeanName = this.toFirstCharLowerCase(beanName);
        return new BeanDefinition(factoryBeanName, beanClassName);
    }

    private String toFirstCharLowerCase(String string) {
        char[] chars = string.toCharArray();
        if (Character.isLowerCase(chars[0])) {
            return string;
        }

        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return this.contextConfig;
    }
}
