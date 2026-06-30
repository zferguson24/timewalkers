package com.wow.timewalkers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Profile("!local")
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${api.key}")
    private String apiKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Security: API key enforcement ACTIVE");
        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new ApiKeyAuthFilter(apiKey), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> { throw new UsernameNotFoundException("Direct user authentication is not supported"); };
    }
}
