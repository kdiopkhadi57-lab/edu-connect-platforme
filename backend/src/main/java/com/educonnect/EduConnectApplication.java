package com.educonnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EduConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduConnectApplication.class, args);
    }
}
