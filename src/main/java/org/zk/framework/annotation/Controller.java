package org.zk.framework.annotation;

import java.lang.annotation.*;

/**
 * 描述: Controller
 * @author kun.zhu
 * @date 2020/12/19 14:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {

    String value() default "";
}
