package org.zk.framework.annotation;

import java.lang.annotation.*;

/**
 * 描述: RequestParam
 * @author kun.zhu
 * @date 2020/12/19 14:01
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    String value() default "";

    boolean required() default true;
}
