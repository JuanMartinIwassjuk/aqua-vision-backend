package com.app.aquavision;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "AquaVision - Backend API",
				version = "1.0",
				description = "Permite dar de alta Hogares y Generar reportes de mediciones por fecha"
		)
)
public class SpringbootAquaVisionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootAquaVisionApplication.class, args);
	}

}
