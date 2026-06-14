package com.wow.timewalkers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication is a convenience annotation that combines:
//   @Configuration      - marks this class as a source of bean definitions
//   @EnableAutoConfiguration - tells Spring Boot to auto-configure the application context
//                              based on dependencies found on the classpath (e.g. sets up JPA
//                              because spring-data-jpa is present)
//   @ComponentScan      - scans this package and all sub-packages for Spring-managed
//                         components (@Service, @Repository, @RestController, etc.)
@SpringBootApplication
public class TimewalkersApplication {

    public static void main(String[] args) {
        // Bootstraps the entire Spring application context and starts the embedded web server
        SpringApplication.run(TimewalkersApplication.class, args);
    }
}
