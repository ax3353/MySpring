package org.zk.framework;

import cn.udream.test.controller.MyController;
import org.zk.framework.context.ApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext("classpath:application.properties");
//        MyController myController = (MyController) applicationContext.getBean("myController");
//        myController.foo("zjy");

        MyController myController1 = (MyController) applicationContext.getBean(MyController.class);
        myController1.first("zk");
    }
}
