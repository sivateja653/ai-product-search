package com.aiproductsearch.search_service.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class WebCorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();

    // Allowed frontend origins
    cfg.setAllowedOrigins(
        List.of(
            "http://localhost:5173", "https://staging-api.eazysearch.shop" // your staging web app
            ));
    // If you need wildcard subdomains, use:
    cfg.setAllowedOriginPatterns(List.of("https://*.eazysearch.shop"));

    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setExposedHeaders(List.of("X-Correlation-Id")); // optional
    cfg.setAllowCredentials(false); // keep false unless you truly need cookies

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
