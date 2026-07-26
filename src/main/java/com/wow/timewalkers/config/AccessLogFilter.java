package com.wow.timewalkers.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Tomcat's file-based AccessLogValve can't write anywhere in Lambda's read-only filesystem.
// This logs the same information via SLF4J instead, which reaches CloudWatch Logs the same
// way every other log line does — no filesystem access needed.
@Component
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long durationMs = System.currentTimeMillis() - start;

        // Cloudflare's own edge IP otherwise shows up as the remote address, since
        // Cloudflare proxies the connection to the Lambda Function URL.
        String clientIp = request.getHeader("CF-Connecting-IP");
        if (clientIp == null) {
            clientIp = request.getRemoteAddr();
        }

        log.info("{} {} {} {} {}ms", clientIp, request.getMethod(), request.getRequestURI(),
                response.getStatus(), durationMs);
    }
}