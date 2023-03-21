package com.secjar.secjarapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@PropertySource("classpath:emailSender.properties")
@PropertySource("classpath:files.properties")
@PropertySource("classpath:hsm.properties")
@PropertySource("classpath:accountCreation.properties")
@PropertySource("classpath:mfa.properties")
@EnableScheduling
@SpringBootApplication
public class SecjarApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecjarApiApplication.class, args);
    }

}
