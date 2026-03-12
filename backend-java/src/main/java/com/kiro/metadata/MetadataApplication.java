package com.kiro.metadata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Metadata Management System Application
 * Main entry point for the Spring Boot application
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@MapperScan("com.kiro.metadata.repository")
public class MetadataApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MetadataApplication.class, args);
    }
}
