package com.lyj.swagger2markdown.config;

import com.lyj.swagger2markdown.ToMarkdown;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableSwagger2 //如果开启生成文档，则开启swagger
@Configuration
@ComponentScan(
        basePackages = {"com.lyj.swagger2markdown.service"}
)
@Import(ToMarkdown.class)
public class SwaggerToMdAutoConfiguration  {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
