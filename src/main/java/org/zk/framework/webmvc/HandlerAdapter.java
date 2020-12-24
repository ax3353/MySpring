package org.zk.framework.webmvc;

import org.zk.framework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HandlerAdapter {

    public boolean supports(Object handler) {
        return handler instanceof HandlerMapping;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMapping handlerMapping = (HandlerMapping) handler;
        Method method = handlerMapping.getMethod();

        // 形参名字与位置映射表
        Map<String, Integer> paramNameIndexMapping = new HashMap<>();

        //提取加了RequestParam注解的参数的位置
        Annotation[][] pas = method.getParameterAnnotations();
        for (int i = 0; i < pas.length; i++) {
            for (Annotation annotation : pas[i]) {
                if (annotation instanceof RequestParam) {
                    String paramName = ((RequestParam) annotation).value();
                    if (!"".equals(paramName)) {
                        paramNameIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //提取request和response的位置
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramNameIndexMapping.put(type.getName(), i);
            }
        }

        //实参列表，要运行时才能拿到值
        Object[] actualParamValues = new Object[parameterTypes.length];
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            if (!paramNameIndexMapping.containsKey(key)) {
                continue;
            }

            String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
            Integer index = paramNameIndexMapping.get(key);
            actualParamValues[index] = this.autoTypeMapping(value, parameterTypes[index]);
        }

        if (paramNameIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int index = paramNameIndexMapping.get(HttpServletRequest.class.getName());
            actualParamValues[index] = request;
        }

        if (paramNameIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int index = paramNameIndexMapping.get(HttpServletResponse.class.getName());
            actualParamValues[index] = response;
        }

        Object o = method.invoke(handlerMapping.getHandler(), actualParamValues);
        if (o == null) {
            return null;
        }

        if (method.getReturnType() == ModelAndView.class) {
            return (ModelAndView) o;
        }
        return null;
    }

    /**
     * 自动类型映射, 简单处理
     */
    private Object autoTypeMapping(String value, Class<?> paramType) {
        if (String.class == paramType) {
            return value;
        }

        if (Integer.class == paramType) {
            return Integer.valueOf(value);
        }

        if (Float.class == paramType) {
            return Float.valueOf(value);
        }

        if (Double.class == paramType) {
            return Double.valueOf(value);
        }

        if (BigDecimal.class == paramType) {
            return new BigDecimal(value);
        }
        return value;
    }
}
