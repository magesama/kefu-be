package com.example.kefu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class MultipartConfig {
    
    @PostConstruct
    public void init() {
        File tempDir = new File("./upload-tmp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
    }
    
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setMaxUploadSize(10485760); // 10MB
        resolver.setMaxInMemorySize(0);
        return resolver;
    }
}