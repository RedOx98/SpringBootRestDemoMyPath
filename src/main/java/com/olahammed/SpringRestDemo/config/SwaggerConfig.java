package com.olahammed.SpringRestDemo.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@Configuration
@OpenAPIDefinition(
        info = @Info(
            title = "Demo API",
            version = "Version 1.0",
            contact = @Contact(
                name = "Olahammed", email = "olaskeet@gmail.com", url = "https://olahammed.org"
            ),
            license = @License(
                name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"
            ),
            termsOfService = "https://studyeasy.org/TOS",
            description = "Spring Boot Restful API demo by olahammed"
        )
    )
public class SwaggerConfig {
    
}
