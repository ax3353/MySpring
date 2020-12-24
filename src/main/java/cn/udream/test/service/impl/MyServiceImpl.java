package cn.udream.test.service.impl;

import cn.udream.test.service.MyService;
import org.zk.framework.annotation.Service;

@Service
public class MyServiceImpl implements MyService {

    @Override
    public String doService(String str) {
        System.out.println("-------doService---------: " + str);
        return "doService::" + str;
    }
}
