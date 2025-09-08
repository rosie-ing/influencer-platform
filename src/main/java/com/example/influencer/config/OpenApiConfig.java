package com.example.influencer.config;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
//Swagger UI에 Authorize 버튼을 등록
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Influencer API").version("v1"))
        // 문서의 모든 API에 기본적으로 인증 필요
        // 실제 접근 허용/ 차단 SecurityConfig 가 결정, 문서 상으론 전부 보호 엔드포인트처럼 보이게 함.
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("http://localhost:8080")
                        .description("Local server"));


    }
}
