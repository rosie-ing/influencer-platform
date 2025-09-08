package com.example.influencer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

//log 자동 생성
@Slf4j
//final 필드를 인자로 받는 생성자 자동생성
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    //JWT를 검증/파싱하는 헬퍼 빈을 DI 받는다.
    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {


        String token = extractBearerToken(request);

        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (!tokenProvider.validateAccessToken(token)) {
                log.warn("JWT reject: invalid token");
                chain.doFilter(request, response);
                return;
            }

            io.jsonwebtoken.Claims  claims = tokenProvider.parseAllClaims(token);
            Long userId = tokenProvider.getUserId(token);

            String email = claims.getSubject();

            var auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

            var details = new java.util.HashMap<String, Object>(claims);
            if (userId != null) details.put("userId", userId);
            auth.setDetails(details);

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            log.warn("JWT processing failed: {}", rootMessage(e));
        }

        chain.doFilter(request, response);
    }

    //Authorization 헤더에서 Bearer 토큰을 안전하게 추출 (대소문자/공백/중복 Bearer 허용)
    private String extractBearerToken(HttpServletRequest request) {
        String h = request.getHeader("Authorization");
        if (!StringUtils.hasText(h)) return null;
        String s = h.trim();
        int idx = s.toLowerCase().indexOf("bearer ");
        if (idx < 0) return null;
        String token = s.substring(idx + "bearer ".length()).trim();
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring("bearer ".length()).trim();
        }
        return token.isEmpty() ? null : token;
    }

    private boolean isNumeric(String v) {
        if (!StringUtils.hasText(v)) return false;
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        String name = cur.getClass().getSimpleName();
        String msg  = (cur.getMessage() != null) ? cur.getMessage() : t.toString();
        return name + ": " + msg;
    }
}
