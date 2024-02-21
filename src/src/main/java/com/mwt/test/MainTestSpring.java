package com.mwt.test;

import com.mwt.spring.DyanmicConfigApplicationContext;
import com.mwt.test.service.DemoService;

/**
 * @ClassNAME MainTestSpring
 * @Description TODO
 * @Author mingwentao
 * @Date 2024/2/21 11:45
 * @Version 1.0
 */
public class MainTestSpring {
    public static void main(String[] args) {
        DyanmicConfigApplicationContext context = new DyanmicConfigApplicationContext(AppConfig.class);
        DemoService service = (DemoService) context.getBean("demoService");
        service.method();
    }
}
