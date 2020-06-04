## 概述

该项目可以将swagger生成的数据转换成markdown格式

该项目可以很好的和SpringBoot结合使用，具体参考以下操作

## 操作

将项目打成jar包，然后添加到对应的项目
通过`@EnableSwaggerToMD`注解来开启生成markdown说明文档
生成的markdown文档位于项目的根路径下，名称为`API.md`

## 原理

通过`@EnableSwaggerToMD`注解来导入自动配置类
在SpringBoot启动完成后会回调`ApplicationRunner`接口
在回调中请求swagger的数据，然后解析并转换成markdown说明文档