package com.example.plantumlwebeditorv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class PlantUmlWebEditorV2Application {
    public static void main(String[] args) {
        SpringApplication.run(PlantUmlWebEditorV2Application.class, args);
    }

}
