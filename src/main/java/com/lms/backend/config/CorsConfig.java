package com.lms.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS config — CRITICAL for HttpOnly cookie to work cross-origin.
 * Must set allowCredentials=true AND specify exact origins (not wildcard).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // MUST be exact origin, NOT "*" when allowCredentials=true
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",  // Vite dev
               "http://localhost:5174",  // Vite dev
                "http://localhost:3000",   // CRA dev (if needed)
                "https://team2-fe-five.vercel.app/"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // CRITICAL: allows browser to send cookies cross-origin
        config.setAllowCredentials(true);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
