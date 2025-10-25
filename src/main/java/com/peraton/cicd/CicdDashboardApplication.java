package com.peraton.cicd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CicdDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(CicdDashboardApplication.class, args);
    }
}
