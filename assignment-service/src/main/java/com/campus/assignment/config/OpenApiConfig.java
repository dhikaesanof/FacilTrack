package com.campus.assignment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assignmentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Assignment Service API")
                        .description("Microservice for Maintenance Assignments, Technician Dispatch, and Work Completion")
                        .version("1.0.0")
                        .contact(new Contact().name("Campus Engineering Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Local Development Server")
                ));
    }
}
