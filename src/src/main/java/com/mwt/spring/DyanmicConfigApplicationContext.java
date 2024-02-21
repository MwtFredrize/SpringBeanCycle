package com.mwt.spring;

import com.mwt.spring.annotation.*;
import com.mwt.spring.entity.BeanDefinition;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * @ClassNAME DyanmicConfigApplicationContext
 * @Description 容器类
 * @Author mingwentao
 * @Date 2024/2/21 11:46
 * @Version 1.0
 */
public class DyanmicConfigApplicationContext {

    private Class appConfig;

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private Map<String, Object> singletonMap = new HashMap<>();


    public DyanmicConfigApplicationContext(Class appConfig) {
        this.appConfig = appConfig;

        //扫描路径，把@service注解标记的对象生成BeanDefinition对象放入缓存Map中
        scanClassSetBean(appConfig);

        //把所有的单例对象生成Bean对象放入单例池（SingletonMap）中，单例是生成一次，后续getBean直接从单例池获取
        //多例每次getBean需要重新生成
        //从BeanDefinitionMap这个包含了所有扫描到Bean描述Map中获取单例Bean，并生成Bean
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            BeanDefinition type = beanDefinitionEntry.getValue();
            Object bean = createBean(beanName, type);
            singletonMap.put(beanName, bean);
        }

    }

    /**
     * 创建Bean的方法
     *
     * @param beanName
     * @param type
     * @return
     */
    private Object createBean(String beanName, BeanDefinition type) {
        //创建Bean class  - 》  构造方法实例化对象   - 》 依赖注入   -》 aware接口回调
        // -》 初始化前 @PostConstruct  - 》 初始化 Init  -》 初始化后 AOP，事务

        Class clazz = type.getType();

        try {
            Object obj = clazz.newInstance();

            //遍历obj的字段，是否被Autowired注解标记
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    //依赖注入，属性赋值
                    Object bean = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(obj, bean);
                }
            }

            return obj;

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private void scanClassSetBean(Class appConfig) {
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

            List<File> fileList = new ArrayList<>();
            //获取目录下的所有class文件
            if (classFileDir.isDirectory()) {
                for (File f : classFileDir.listFiles()) {
                    if (f.isDirectory()) {
                        for (File f1 : f.listFiles()) {
                            if (!f1.isDirectory()) {
                                fileList.add(f1);
                            }
                        }
                    } else {
                        fileList.add(f);
                    }
                }
            } else {
                throw new Error("扫描类文件目录异常：" + classFileDir.getAbsolutePath());
            }

            //文件转换，把文件目录下的文件转换成class对象
            for (File file : fileList) {
                String absolutePath = file.getAbsolutePath();
                String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"))
                        .replace("/", ".");

                try {
                    //app类加载器加载扫描到的class类
                    Class<?> aClazz = appClassLoader.loadClass(className);
                    //被@Service注解标记的类才需要生成Bean对象，让Spring容器管理
                    if (aClazz.isAnnotationPresent(Service.class)) {
                        //生成BeanDefinition对象，让Spring容器对Bean对象管理
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setType(aClazz);
                        //判断class对象是否被懒加载注解标记
                        beanDefinition.setLazy(aClazz.isAnnotationPresent(Lazy.class));
                        //判断对象是单例Bean还是原型Bean，对象上的作用域@Scope
                        if (aClazz.isAnnotationPresent(Scope.class)) {
                            //如果添加了@Scope注解，就直接把值设置到BeanDefinition中
                            beanDefinition.setScope(aClazz.getAnnotation(Scope.class).value());
                        } else {
                            //如果没有添加说明是单例
                            beanDefinition.setScope("singleton");
                        }

                        //获取BeanName，用于key存放在Map中
                        String beanName = aClazz.getAnnotation(Service.class).value();
                        if (beanName.isEmpty()) {
                            beanName = Introspector.decapitalize(aClazz.getSimpleName());
                        }
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

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
        BeanDefinition beanDefinition = beanDefinitionMap.get(demoService);

        //如果是单例直接从单例池获取，多例调用createBean方法重新创建
        if (beanDefinition.getScope().isEmpty() || "singleton".equals(beanDefinition.getScope())) {
            Object o = singletonMap.get(demoService);
            //如果依赖注入的时候还没有数据，这里生成Bean放入单例池
            if (o == null) {
                o = createBean(demoService, beanDefinition);
                singletonMap.put(demoService, o);
            }
            return o;
        } else {
            //多例
            return createBean(demoService, beanDefinition);
        }
    }
}
