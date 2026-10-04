package com.yuchen.kami.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String scheme = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("YU-Kami API")
                        .description("屿宸科技 · 企业级卡密系统 RESTful API 文档")
                        .version("1.14.0")
                        .contact(new Contact().name("YU-Kami").url("https://github.com/Ms-liyc/YU-Kami"))
                        .license(new License().name("MIT").url("https://github.com/Ms-liyc/YU-Kami/blob/main/LICENSE")))
                .components(new Components().addSecuritySchemes(scheme, new SecurityScheme()
                        .name(scheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("管理端或商城登录后获取的 JWT Token")))
                .addSecurityItem(new SecurityRequirement().addList(scheme));
    }
}
