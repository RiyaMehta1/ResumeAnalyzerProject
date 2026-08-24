package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.nio.file.Paths;

@SpringBootApplication
public class ResumeAnalyzerBackendApplication {

    public static void main(String[] args) {
        // Load .env file from project root directory BEFORE Spring initializes
        String projectRoot = Paths.get("").toAbsolutePath().getParent().getParent().toString();
        String envFilePath = Paths.get(projectRoot, ".env").toString();
        
        Dotenv dotenv = Dotenv.configure()
                .directory(projectRoot)
                .filename(".env")
                .ignoreIfMissing()
                .load();

        // Set all environment variables from .env into System properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            System.out.println("Loaded env: " + entry.getKey());
        });

        SpringApplication.run(ResumeAnalyzerBackendApplication.class, args);
    }

}
