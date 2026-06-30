package com.wow.timewalkers.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final String apiKey;

    public ApiKeyAuthFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        log.debug("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            if (apiKey.equals(token)) {
                log.debug("API key authentication successful");
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("api-client", null, List.of())
                );
            } else {
                log.warn("Rejected request — invalid API key on {} {}", request.getMethod(), request.getRequestURI());
            }
        } else {
            log.warn("Rejected request — missing Authorization header on {} {}", request.getMethod(), request.getRequestURI());
        }
        chain.doFilter(request, response);
    }
}
