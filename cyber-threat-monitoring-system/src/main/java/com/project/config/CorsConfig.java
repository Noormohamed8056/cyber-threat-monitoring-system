package com.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig {

    // ==========================================
    // ALLOWED ORIGINS (REACT FRONTEND PORTS)
    // ==========================================
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",   // React default port
            "http://localhost:5173",   // Vite React port
            "http://localhost:8080",   // Same server
            "http://localhost:4200"    // Angular (if needed)
    );

    // ==========================================
    // ALLOWED HTTP METHODS
    // ==========================================
    private static final List<String> ALLOWED_METHODS = Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
    );

    // ==========================================
    // ALLOWED HEADERS
    // ==========================================
    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With"
    );

    // ==========================================
    // EXPOSED HEADERS (VISIBLE TO FRONTEND)
    // ==========================================
    private static final List<String> EXPOSED_HEADERS = Arrays.asList(
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
    );

    // ==========================================
    // CORS CONFIGURATION SOURCE BEAN
    // ==========================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Set allowed origins
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);

        // Set allowed HTTP methods
        configuration.setAllowedMethods(ALLOWED_METHODS);

        // Set allowed headers
        configuration.setAllowedHeaders(ALLOWED_HEADERS);

        // Set exposed headers
        configuration.setExposedHeaders(EXPOSED_HEADERS);

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour (3600 seconds)
        configuration.setMaxAge(3600L);

        // Apply CORS config to all endpoints
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // ==========================================
    // CORS FILTER BEAN
    // ==========================================
    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins for filter level
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);

        // Allow all standard methods
        configuration.setAllowedMethods(ALLOWED_METHODS);

        // Allow all headers
        configuration.setAllowedHeaders(ALLOWED_HEADERS);

        // Expose headers to frontend
        configuration.setExposedHeaders(EXPOSED_HEADERS);

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Register for all paths
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }
}
