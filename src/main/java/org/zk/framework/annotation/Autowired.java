package org.zk.framework.annotation;

import java.lang.annotation.*;

/**
 * 描述: Autowired
 * @author kun.zhu
 * @date 2020/12/19 14:01
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowired {

    String value() default "";
}
