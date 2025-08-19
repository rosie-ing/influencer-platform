package com.example.influencer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    //개발 초기 편의를 위해 임시 오픈
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws  Exception{
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/ping",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/health",
                                "/actuator/health",
                                "/auth/**"//회원가입 로그인 시 사용
                        ).permitAll()
                        .anyRequest().permitAll() //개발 초반 전체 오픈
                        //.anyRequest().authenticated() + 필터추가
                );
        return http.build();
    }
}
