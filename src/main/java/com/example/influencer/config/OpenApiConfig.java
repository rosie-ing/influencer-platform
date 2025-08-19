package com.example.influencer.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
   @Bean
    public OpenAPI openAPI(){
       return new OpenAPI()
               .info(new Info()
                   .title("Influencer Platform API")
                   .description("MVP 백엔드 API 문서")
                   .version("v0.1")
               .contact(new Contact().name("Team").email("team@example.com")
               )
       );

   }
}

