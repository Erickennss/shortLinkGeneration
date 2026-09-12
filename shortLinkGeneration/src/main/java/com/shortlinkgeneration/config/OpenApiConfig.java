package com.shortlinkgeneration.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("分布式短链系统 API")
                        .version("1.0")
                        .description("基于Spring Boot 3.2.12实现的分布式短链生成系统"));
    }
}