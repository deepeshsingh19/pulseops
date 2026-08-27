package com.pulseops.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PulseOpsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PulseOpsApiApplication.class, args);
    }
}