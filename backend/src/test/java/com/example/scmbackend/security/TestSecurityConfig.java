package com.example.scmbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/warehouses", "/api/categories", "/api/suppliers", "/api/products").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/purchase-orders/**", "/api/transfers/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/purchase-orders/**", "/api/transfers/**", "/api/inventory/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/sales-orders/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/api/sales-orders/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "STAFF")
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
