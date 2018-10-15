package com.sunsan.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.collect.Lists.newArrayList;

@Configuration
@EnableSwagger2
@EnableWebMvc
public class SwaggerConfig extends  WebMvcConfigurerAdapter{
    @Bean
    public Docket createRestApi() {
    	
        return new Docket(DocumentationType.SWAGGER_2)
                .select() 
                .apis(RequestHandlerSelectors.basePackage("com.sunsan.project.controller"))
                .paths(PathSelectors.any())
                .build()
                .directModelSubstitute(java.sql.Timestamp.class, String.class)
                .securitySchemes(newArrayList(apiKey()))
                .apiInfo(apiInfo());
    }
    
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("北京顺山软件有限公司")
                .description("杨志强")
                .termsOfServiceUrl(" API terms of service")
                .version("1.0.0")
                .build();
    }
    
    @Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

    @Bean
    SecurityScheme apiKey() {
        return new ApiKey("api_key", "api_key", "header");
    }
}
