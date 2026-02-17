package com.axiom.RagChat.config;

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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RAG Chat Storage API")
                .version("1.0.0")
                .description("Production-ready RAG Chat Storage Microservice with lifecycle-aware AI memory compression")
                .contact(new Contact()
                    .name("Axiom Systems")
                    .email("support@axiom-systems.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://axiom-systems.com/license")))
            .addSecurityItem(new SecurityRequirement()
                .addList("bearerAuth")
                .addList("apiKey"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
                .addSecuritySchemes("apiKey", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-Key")));
    }
}