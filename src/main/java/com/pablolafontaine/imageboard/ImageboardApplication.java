package com.pablolafontaine.imageboard;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableAsync
public class ImageboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageboardApplication.class, args);
    }

    @Value("${ui.host}")
    private String uiHost;

    @Value("${ui.port}")
    private String uiPort;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Imageboard-");
        executor.initialize();
        return executor;
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(String.format("http://%s:%s", 
                             uiHost, uiPort))
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("Authorization", "Accept", "Content-Type")
                        .maxAge(3600);
            }
        };
    }
}