package com.mwt.spring.entity;

/**
 * @ClassNAME BeanDefinition
 * @Description Spring容器管理
 * @Author mingwentao
 * @Date 2024/2/21 23:07
 * @Version 1.0
 */
public class BeanDefinition {

    /**
     * bean对象类型
     */
    private Class type;

    /**
     * 作用域：单例还是原型
     */
    private String scope;

    /**
     * 是否懒加载
     */
    private Boolean isLazy;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getLazy() {
        return isLazy;
    }

    public void setLazy(Boolean lazy) {
        isLazy = lazy;
    }
}
