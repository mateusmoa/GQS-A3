package com.nutriapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NutriAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(NutriAppApplication.class, args);
        System.out.println("NutriApp iniciado com sucesso!");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("API Docs: http://localhost:8080/api-docs");
    }
}