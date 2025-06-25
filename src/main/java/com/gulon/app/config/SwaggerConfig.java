package com.gulon.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.gulon.com").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .schemaRequirement("JWT", jwtSecurityScheme());
    }

    private Info apiInfo() {
        return new Info()
                .title("Gulon API Documentation")
                .description("독서 모임 및 소셜 도서 관리 플랫폼 API 문서입니다. " +
                            "이 API를 통해 사용자 관리, 그룹 관리, 도서 관리, 채팅 기능 등을 제공합니다.")
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Gulon Development Team")
                        .email("dev@gulon.com")
                        .url("https://gulon.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT 토큰을 입력하세요. (Bearer 접두사 제외)");
    }
} 