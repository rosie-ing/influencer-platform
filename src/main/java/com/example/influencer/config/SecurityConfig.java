package com.example.influencer.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(anon -> anon.disable()) //익명 인증 끄기 : 미인증은 401로
                .exceptionHandling(ex -> ex
                        // 인증이 없을 때는 무조건 401
                        .authenticationEntryPoint((req, res, e) -> {
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/health",
                                "/actuator/health",
                                "/auth/refresh"
                        ).permitAll()

                        //공개 엔드포인트 : 로그인/ 회원가입만
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // 관리자 전용
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        //임시
                        .requestMatchers("/auth/check-email").permitAll()
                        .requestMatchers("/auth/check-nickname").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()

                        //프리플라이트 허용(예방 차원)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 나머지는 모두 인증 필요 (여기에 /auth/me, /interests/** 포함)
                        .anyRequest().authenticated()


                )
                .addFilterBefore(new JwtAuthFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
