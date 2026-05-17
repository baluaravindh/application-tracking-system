package com.balu.application_tracking_system;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApplicationTrackingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationTrackingSystemApplication.class, args);
	}

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Application Tracking System")
						.description("ATS API Documentation")
						.version("1.0"))
				.addSecurityItem(new SecurityRequirement()
						.addList("Bearer Authentication"))
				.components(new Components()
						.addSecuritySchemes("Bearer Authentication",
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")));
	}
}
