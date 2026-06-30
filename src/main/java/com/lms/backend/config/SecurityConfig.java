package com.lms.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Bắt buộc phải có dòng này để @PreAuthorize trong Controller hoạt động
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để dùng Postman không bị lỗi
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // Tạm thời mở khóa tất cả API để dễ test
                );
        return http.build();
    }
}