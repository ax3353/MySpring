package org.zk.framework.webmvc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandlerMapping {

    /**
     * 保存方法对应的实例
     */
    private Object handler;

    /**
     * 保存映射的方法
     */
    private Method method;

    /**
     * URL的正则匹配
     */
    private Pattern pattern;
}
