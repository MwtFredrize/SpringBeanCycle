package com.mwt.spring;

import com.mwt.spring.annotation.ComponentScan;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;

/**
 * @ClassNAME DyanmicConfigApplicationContext
 * @Description 容器类
 * @Author mingwentao
 * @Date 2024/2/21 11:46
 * @Version 1.0
 */
public class DyanmicConfigApplicationContext {

    private Class appConfig;

    public DyanmicConfigApplicationContext(Class appConfig) {
        this.appConfig = appConfig;

        //1. 拿到路径
        if (appConfig.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan annotation = (ComponentScan) appConfig.getAnnotation(ComponentScan.class);
            /**
             * 这里path路径只是java类的路径，实际上需要的是class文件路径，根据类加载器可以获得实际target下的class文件路径
             */
            String path = annotation.value().replace(".", "/");

            //应用类加载器加载获取当前路径下的class文件路径
            ClassLoader appClassLoader = this.getClass().getClassLoader();
            URL resource = appClassLoader.getResource(path);
            File classFileDir = new File(resource.getFile());

            if (classFileDir.isDirectory()) {
                for (File classFile : classFileDir.listFiles()) {
                    System.out.println("classFile = " + classFile.getAbsoluteFile());
                }
            } else {
                throw new Error("class文件夹路径异常");
            }

        }

    }


    /**
     * 获取Bean的方法
     *
     * @param demoService
     * @return
     */
    public Object getBean(String demoService) {
        return null;
    }
}
