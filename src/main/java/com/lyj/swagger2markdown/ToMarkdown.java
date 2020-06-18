package com.lyj.swagger2markdown;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Optional;
import com.lyj.swagger2markdown.model.ModelAttr;
import com.lyj.swagger2markdown.model.Request;
import com.lyj.swagger2markdown.model.Table;
import com.lyj.swagger2markdown.param.GroupParam;
import com.lyj.swagger2markdown.service.ProcessService;
import io.swagger.models.Swagger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j
public class ToMarkdown implements ApplicationRunner {
    private String host = "localhost";

    @Value("${server.port}")
    private int port;

    @Autowired
    private DocumentationCache documentationCache;
    @Autowired
    private ServiceModelToSwagger2Mapper mapper;
    @Autowired
    private JsonSerializer jsonSerializer;

    @Autowired
    ProcessService processService;

    //入口
    //服务启动完成后，开始生成md文件
    @Override
    public void run(ApplicationArguments args) {
        try {
            swaggerToMarkdown(GroupParam.groupNames);
        } catch (IOException e) {
            log.error("生成文档异常：", e);
        }
    }

    public void swaggerToMarkdown(String[] groupNames) throws IOException {

        StringBuilder sb = new StringBuilder();

        //如果groupNames不为空，优先使用
        if (groupNames != null && groupNames.length>0) {
            Arrays.stream(groupNames).forEach(groupName -> buildMarkdown(groupName, sb));
        }else{ //如果为空，则使用默认
            Map<String, Documentation> map = documentationCache.all();
            Iterator<String> iterator = map.keySet().iterator();
            while(iterator.hasNext()){
                String groupName = map.get(iterator.next()).getGroupName();
                buildMarkdown(groupName, sb);
            }
        }

        //生成文本
        FileWriter fileWriter = new FileWriter("API.md");
        fileWriter.write(sb.toString());
        fileWriter.close();
    }

    // 获取不同分组的swagger信息
    private String getDocumentation(String swaggerGroup) {
        String groupName = Optional.fromNullable(swaggerGroup).or(Docket.DEFAULT_GROUP_NAME);
        Documentation documentation = documentationCache.documentationByGroup(groupName);
        if (documentation == null) {
            return "";
        }
        Swagger swagger = mapper.mapDocumentation(documentation);
        swagger.basePath("/");
        swagger.host(String.format("%s:%d", host, port));
        return jsonSerializer.toJson(swagger).value();
    }

    private StringBuilder buildMarkdown(String groupName, StringBuilder sb) {
        //拿到swagger所有数据
        String json = getDocumentation(groupName);
        Map controllerMap = (Map) processService.tableListFromString(json).get("tableMap");

        if (controllerMap == null) {
            String url = String.format("http://%s:%d/v2/api-docs?group=%s", host, port, groupName);
            log.error("请检查Swagger地址：" + url + ",没有数据！");
            return sb;
        }
        //解析生成markdown
        return buildMarkdown(sb, controllerMap);
    }

    private StringBuilder buildMarkdown(StringBuilder sb, Map<String, Object> controllerMap) {

        Iterator iterator = controllerMap.keySet().iterator();
        while (iterator.hasNext()) {
            ArrayList<Table> method = (ArrayList<Table>) controllerMap.get(iterator.next());

            for (Table table : method) {
                sb.append("**简要描述：**\n\n");
                sb.append(String.format("- %s\n\n", table.getDescription()));
                sb.append("**请求URL：**\n\n");
                sb.append(String.format("- ` %s `\n\n", table.getUrl()));
                sb.append("**请求方式：**\n\n");
                sb.append(String.format("- %s\n\n", table.getRequestType()));
                sb.append("**请求参数：**\n\n");
                List<Request> requestList = table.getRequestList();
                sb.append(String.format("%s\n\n", buildRequestTable(requestList)));
                sb.append("**请求示例**\n\n");
                sb.append(buildExample(table.getRequestParam()));
                sb.append("**返回示例**\n\n");
                sb.append(buildExample(table.getResponseParam()));
                sb.append("**返回参数说明**\n\n");
                List<ModelAttr> properties = table.getModelAttr().getProperties();
                sb.append(String.format("%s\n\n", buildResponseTable(properties)));
                sb.append("**备注**\n\n");
                sb.append("- 更多返回错误代码请看首页的错误代码描述\n\n");
                sb.append("---\n\n");
            }

        }
        return sb;
    }

    private String buildExample(String s) {
        Object param = null;

        if (s == null || "".equals(s)) {
            return "- 无\n\n";
        }

        //Post
        if (s.startsWith(" -d ")) {
            int start = s.indexOf("{");
            int end = s.lastIndexOf("}") + 1;
            s = s.substring(start, end);
            try {
                param = JSON.parse(s);
            } catch (Exception e) {
                log.error("生成文档异常：", e);
            }

        }
        //JSON
        else if (s.startsWith("{") && s.endsWith("}")) {
            param = JSON.parse(s);
        }
        //get
        else {
            String[] split = s.split("&");
            HashMap<Object, Object> map = new HashMap<>();
            for (String str : split) {
                String[] split1 = str.split("=");
                if (split1.length == 1) {
                    map.put(split1[0], "");
                    continue;
                }
                map.put(split1[0], split1[1]);
            }
            param = map;
        }

        return String.format("```java\n%s\n```\n\n", JSON.toJSONString(param, SerializerFeature.PrettyFormat));
    }


    //构建参数列表
    public String buildResponseTable(List<ModelAttr> parameters) {
        if (parameters.isEmpty()) {
            return "- 无\n\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("|参数名|类型|说明|\n");
        sb.append("|----|----|-----|-----|\n");
        parameters.forEach(parameter -> {
            //参数名，类型，说明
            sb.append(String.format("|%s|%s|%s|\n",
                    parameter.getName(),
                    parameter.getType(),
                    parameter.getDescription() == null ? "null" : parameter.getDescription())
            );
        });
        sb.append("\n");
        return sb.toString();
    }

    //构建参数列表
    public String buildRequestTable(List<Request> parameters) {
        if (parameters.isEmpty()) {
            return "- 无\n\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("|参数名|必选|类型|说明|\n");
        sb.append("|----|----|-----|-----|\n");
        parameters.forEach(parameter -> {
            //参数名，必选，类型，说明
            sb.append(String.format("|%s|%s|%s|%s|\n",
                    parameter.getName(),
                    parameter.getRequire(),
                    parameter.getParamType(),
                    parameter.getRemark() == null ? "null" : parameter.getRemark())
            );
        });
        sb.append("\n");
        return sb.toString();
    }


}
