package org.zk.framework.annotation;

import java.lang.annotation.*;

/**
 * 描述: Service
 * @author kun.zhu
 * @date 2020/12/19 14:01
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {

    String value() default "";
}
