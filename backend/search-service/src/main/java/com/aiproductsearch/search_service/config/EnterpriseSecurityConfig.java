package com.aiproductsearch.search_service.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Profile({"prod", "enterprise"})
public class EnterpriseSecurityConfig {

  @Bean
  SecurityFilterChain enterpriseFilterChain(HttpSecurity http, JwtAuthenticationConverter conv)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health", "/actuator/health/**")
                    .permitAll() // <-- fixed leading slash
                    .requestMatchers("/actuator/**")
                    .hasAuthority("SCOPE_read:admin")
                    .requestMatchers("/api/admin/**")
                    .hasAuthority("SCOPE_read:admin")
                    .requestMatchers("/api/products/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(conv)));
    return http.build();
  }

  @Bean
  JwtAuthenticationConverter jwtAuthConverter() {
    var conv = new JwtAuthenticationConverter();
    conv.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          var out = new ArrayList<GrantedAuthority>();

          // Auth0 RBAC: permissions array → map to SCOPE_*
          Object permsObj = jwt.getClaims().get("permissions");
          if (permsObj instanceof List<?> perms) {
            for (Object p : perms) {
              if (p instanceof String s && !s.isBlank()) {
                out.add(new SimpleGrantedAuthority("SCOPE_" + s));
              }
            }
          }

          // Also honor standard space-delimited "scope" if present
          Object scopeObj = jwt.getClaims().get("scope");
          if (scopeObj instanceof String scope && !scope.isBlank()) {
            for (String s : scope.split(" ")) {
              out.add(new SimpleGrantedAuthority("SCOPE_" + s));
            }
          }
          return out;
        });
    return conv;
  }
}
