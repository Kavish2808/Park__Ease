
package com.parkease.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration c = new CorsConfiguration();

        c.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "null"
        ));

        c.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        c.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource s =
                new UrlBasedCorsConfigurationSource();

        s.registerCorsConfiguration("/api/**", c);

        return s;
    }
}
