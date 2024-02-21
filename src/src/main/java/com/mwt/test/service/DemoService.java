package com.mwt.test.service;

import com.mwt.spring.annotation.Service;

import javax.xml.ws.ServiceMode;

/**
 * @ClassNAME DemoService
 * @Description demoServcie
 * @Author mingwentao
 * @Date 2024/2/21 11:51
 * @Version 1.0
 */
@Service("demoService")
public class DemoService {

    public void method() {
        System.out.println("DemoService method run ...");
    }

}
