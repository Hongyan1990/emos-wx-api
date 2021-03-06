package com.example.emos.wx.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket CreateDoket() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);

        // ApiInfoBuilder 用于在swagger界面上添加各种信息
        ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title("EMOS在线办公系统");
        ApiInfo apiInfo = builder.build();
        docket.apiInfo(apiInfo);

        // ApiSelectorBuilder 用来设置哪些类中的方法会生成到REST Api中
        ApiSelectorBuilder selectorBuilder = docket.select();
        selectorBuilder.paths(PathSelectors.any());
        // 使用@ApiOperation的方法会被提取到Rest Api中
        selectorBuilder.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        docket = selectorBuilder.build();

        // 下面的语句是开启对jwt支持， 当用户用swagger调用受jwt认证保护的方法时，必须要先提交参数（例如令牌）

        // 存储用户必须提交的参数
        List<ApiKey> apiKeyList = new ArrayList<ApiKey>();
        // 规定用户需要输入什么参数
        apiKeyList.add(new ApiKey("token", "token", "header"));
        docket.securitySchemes(apiKeyList);

        // 如果用户认证通过，则在swagger中全局有效
        AuthorizationScope scope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] scopeArray = {scope};
        // 存储令牌的作用域
        SecurityReference reference = new SecurityReference("token", scopeArray);
        List refList = new ArrayList();
        refList.add(reference);
        SecurityContext context = SecurityContext.builder().securityReferences(refList).build();
        List ctxList = new ArrayList();
        ctxList.add(context);
        docket.securityContexts(ctxList);

        return docket;
    }
}
