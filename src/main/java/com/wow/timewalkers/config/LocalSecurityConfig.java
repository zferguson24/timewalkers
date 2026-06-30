package com.wow.timewalkers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Profile("local")
@Configuration
@EnableWebSecurity
public class LocalSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(LocalSecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Security: all requests permitted (local profile — no API key required)");
        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
