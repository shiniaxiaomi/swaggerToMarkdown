package com.lyj.swagger2markdown.annotation;

import com.lyj.swagger2markdown.config.SwaggerToMdAutoConfiguration;
import com.lyj.swagger2markdown.param.URLParam;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({SwaggerToMdAutoConfiguration.class, URLParam.class})
public @interface EnableSwaggerToMD {

    String[] url() default "";

}
