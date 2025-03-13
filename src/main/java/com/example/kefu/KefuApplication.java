package com.example.kefu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KefuApplication {

    public static void main(String[] args) {
        SpringApplication.run(KefuApplication.class, args);
    }
}
