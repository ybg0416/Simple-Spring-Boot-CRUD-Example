package io.ybg.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "Simple Spring Boot CRUD",
                description = "Member CRUD 기능",
                version = "v1"))
@Configuration
public class SwaggerConfig {
}