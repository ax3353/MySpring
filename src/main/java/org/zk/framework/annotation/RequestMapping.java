package org.zk.framework.annotation;

import java.lang.annotation.*;

/**
 * 描述: RequestMapping
 * @author kun.zhu
 * @date 2020/12/19 14:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface RequestMapping {

    String value() default "";
}
