package com.lyj.swagger2markdown.config;

import com.lyj.swagger2markdown.ToMarkdown;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;


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
