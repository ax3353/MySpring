package cn.udream.test.controller;

import cn.udream.test.service.MyService;
import org.zk.framework.annotation.Autowired;
import org.zk.framework.annotation.Controller;
import org.zk.framework.annotation.RequestMapping;
import org.zk.framework.annotation.RequestParam;
import org.zk.framework.webmvc.ModelAndView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class MyController {

    @Autowired
    private MyService myService;

    @RequestMapping("/first")
    public ModelAndView first(@RequestParam("teacher") String teacher) {
        String result = myService.doService(teacher);

        Map<String, Object> model = new HashMap<>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new ModelAndView("first.html", model);
    }

    @RequestMapping("/second")
    public ModelAndView second(@RequestParam("a") Integer a, @RequestParam("b") BigDecimal b) {
        Map<String, Object> model = new HashMap<>();
        model.put("teacher", a);
        model.put("data", b);
        model.put("token", "09876");
        return new ModelAndView("a/second.html", model);
    }
}
