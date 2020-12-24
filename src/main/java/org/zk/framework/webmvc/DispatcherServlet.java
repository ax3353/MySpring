package org.zk.framework.webmvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zk.framework.annotation.Controller;
import org.zk.framework.annotation.RequestMapping;
import org.zk.framework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述: <>
 *
 * @author kun.zhu
 * @date 2020/12/19 17:03
 */
public class DispatcherServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(DispatcherServlet.class);

    private final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private List<HandlerMapping> handlerMappings;

    private List<ViewResolver> viewResolvers;

    private Map<HandlerMapping, HandlerAdapter> handlerAdapters;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            this.doDispatch(req, resp);
        } catch (Exception e) {
            LOG.error("Server internal exception", e);
            this.serverInternalException(e, req, resp);
        }
    }

    @Override
    public void init(ServletConfig config) {
        ApplicationContext context = new ApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));
        this.initStrategies(context);
    }

    /**
     * 核心：初始化9大组件
     */
    private void initStrategies(ApplicationContext context) {
        //handlerMapping，必须实现
        this.initHandlerMappings(context);

        //初始化参数适配器，必须实现
        this.initHandlerAdapters(context);

        //初始化视图转换器，必须实现
        this.initViewResolvers(context);
    }

    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = new ArrayList<>();

        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object controllerBean = context.getBean(beanName);
            Class<?> controllerBeanClz = controllerBean.getClass();
            if (!controllerBeanClz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            String baseUrl = controllerBeanClz.isAnnotationPresent(RequestMapping.class) ?
                    controllerBeanClz.getAnnotation(RequestMapping.class).value() : "";

            for (Method method : controllerBeanClz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }

                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                String url = ("/" + baseUrl + "/" + annotation.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                this.handlerMappings.add(new HandlerMapping(controllerBean, method, Pattern.compile(url)));
                LOG.info("Mapped {} to {}", url, method);
            }
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = new HashMap<>();

        //把request请求变成一个handler，参数都是字符串的，自动配到handler中的形参
        //可想而知，他要拿到HandlerMapping才能干活
        //就意味着，有几个HandlerMapping就有几个HandlerAdapter
        for (HandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new HandlerAdapter());
        }
    }

    private void initViewResolvers(ApplicationContext context) {
        this.viewResolvers = new ArrayList<>();

        //拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for (String template : templateRootDir.list()) {
            //这里主要是为了兼容多模板，所以模仿Spring用List保存
            //在我写的代码中简化了，其实只有需要一个模板就可以搞定
            //只是为了仿真，所有还是搞了个List
            this.viewResolvers.add(new ViewResolver(templateRoot));
        }
    }

    /**
     * 处理请求分发实际入口方法
     */
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //1、通过从request中拿到URL，去匹配一个HandlerMapping
        HandlerMapping mappedHandler = this.getHandler(request);

        //没有找到mapping则报404
        if (mappedHandler == null) {
            this.processDispatchResult(request, response, new ModelAndView("404"), null);
            return;
        }

        //2、准备调用前的参数
        HandlerAdapter handlerAdapter = this.getHandlerAdapter(mappedHandler);

        //3、真正的调用方法,返回ModelAndView存储了要传页面上值和页面模板的名称
        ModelAndView mv = handlerAdapter.handle(request, response, mappedHandler);

        //4、真正的输出调用
        this.processDispatchResult(request, response, mv, null);
    }

    private HandlerMapping getHandler(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String url = request.getRequestURI().replace(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping mapping : this.handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            //如果匹配到直接返回，否则继续下一个匹配
            if (matcher.matches()) {
                return mapping;
            }
        }
        return null;
    }

    protected HandlerAdapter getHandlerAdapter(HandlerMapping handler) throws ServletException {
        if (this.handlerAdapters != null) {
            HandlerAdapter adapter = this.handlerAdapters.get(handler);
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
                                       ModelAndView mv, Exception exception) throws Exception {
        if (exception != null) {
            LOG.error("ProcessDispatchResult exception", exception);
            this.serverInternalException(exception, request, response);
            return;
        }

        if (mv == null) {
            return;
        }

        //把ModelAndView变成一个HTML、outputStream、json、freemarker、velocity
        this.render(mv, request, response);
    }

    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
        View view = this.resolveViewName(mv.getViewName(), mv.getModel(), null, request);
        view.render(mv.getModel(), request, response);
    }

    protected View resolveViewName(String viewName, Map<String, ?> model,
                                   Locale locale, HttpServletRequest request) throws Exception {
        if (this.viewResolvers != null) {
            for (ViewResolver viewResolver : this.viewResolvers) {
                View view = viewResolver.resolveViewName(viewName, locale);
                if (view != null) {
                    return view;
                }
            }
        }
        return null;
    }

    private void serverInternalException(Exception e, HttpServletRequest req, HttpServletResponse resp) {
        Map<String, Object> model = new HashMap<>(2);
        model.put("detail", "Server internal exception");
        model.put("stackTrace", Arrays.toString(e.getStackTrace()));
        try {
            this.render(new ModelAndView("500", model), req, resp);
        } catch (Exception ignored) {
        }
    }
}
