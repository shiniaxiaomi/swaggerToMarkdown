package com.lyj.swagger2markdown.param;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class GroupParam implements ImportBeanDefinitionRegistrar {

    //使用static的原因是不让其被重新初始化，覆盖原始值
    public static String[] groupNames;

    //在导入类的时候回调，用于获取Enable注解的值
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        groupNames= (String[]) importingClassMetadata.
                getAnnotationAttributes("com.lyj.swagger2markdown.annotation.EnableSwaggerToMD")
                .get("groupNames");
    }

}
