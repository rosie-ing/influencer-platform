package com.example.influencer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        //CORS 정책을 정의할 객체 생성
        CorsConfiguration cfg = new CorsConfiguration();

        // 프론트 주소 등록
        // 허용할 Origin 지정 -> 현재 React 개발 서버
        cfg.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));

        // 허용할 HTTP 메서드 지정
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 요청 시 허용할 헤더 지정
        // * -> 모든 요청 헤더 혀용
        cfg.setAllowedHeaders(List.of("*"));

        // 브라우저가 응답에서 접근할 수 있는 헤더 지정
        // 브라우저는 Authorization같은 민감헤더를 차단, 여기서는 노출 허용
        // 예 : 프론트엔드가 응답 헤더의 Authorization: Bearer를 읽을 수 있음.
        cfg.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // 쿠키, Authorization 헤더 같은 인증 정보를 포함할 수 있도록 허용
        // 이 옵션이 true면 setAllowedOrigins("*")는 사용할 수 없음.(보안 문제)
        cfg.setAllowCredentials(true);

        // 프리플라이트 요청 캐싱 시간(초 단위)
        // 3600초 = 1시간
        // 브라우저가 OPTIONS 요청을 매번 보내지 않고, 한 번 성공 시 1시간 동안 재사용
        cfg.setMaxAge(3600L);

        //URL 패턴 기반으로 CORS 정책을 등록할 객체 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //애플리케이션의 모든 API 경로("/**)에 위에서 정의한 CORS 정책(cfg) 적용
        source.registerCorsConfiguration("/**", cfg);

        //최종적으로 CorsConfigurationSource Bean 반환
        // -> 스프링이 글로벌 CORS 설정으로 사용
        return source;
    }
}
