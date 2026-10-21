package com.campus.report.config;

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
    public OpenAPI reportOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Report Service API")
                        .description("Microservice for Campus Facility Complaint Reports and Lifecycle Tracking")
                        .version("1.0.0")
                        .contact(new Contact().name("Campus Engineering Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development Server")
                ));
    }
}
