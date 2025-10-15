package com.aiproductsearch.search_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
@EnableMethodSecurity
public class ProdSecurityConfig {

  @Bean
  SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("actuator/health")
                    .permitAll()
                    // .requestMatchers("/actuator/**")
                    // .hasRole("ADMIN")
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/products/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    /*.anyRequest().authenticated())
    .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));*/
    // needs issuer-uri in prod
    return http.build();
  }
}
