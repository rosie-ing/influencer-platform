package com.example.influencer.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

//스프링 빈으로 등록됨.
@Component
public class JwtTokenProvider {

    //HS256 서명에 쓸 대칭키
    private final Key key;
    //토큰 발급자
    private final String issuer;
    //액세스 토큰 유효기간
    private final long validitySeconds;

    public JwtTokenProvider(
            //application.yml의 값 주입
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.access-token-validity-seconds}") long validitySeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        //이 토큰을 누가 발급 했는지
        this.issuer = issuer;
        this.validitySeconds = validitySeconds;

    }

    //액세스 토큰 생성
    public String createToken(String subject, Map<String, Object> claims) {
        //현재 시각을 구함.
        Instant now = Instant.now();
        return Jwts.builder()
                // 클레임 맵 전체 세팅
                .setClaims(claims)
                // 누구에게 발급한 토큰인가 (보통 이메일)
                .setSubject(subject)
                // 누가 이 토큰을 발급했는가
                .setIssuer(issuer)
                // UTC 기준 절대 시각
                .setIssuedAt(Date.from(now))
                // 토큰 만료 시각
                .setExpiration(Date.from(now.plusSeconds(validitySeconds)))
                //어떤 키와 알고리즘을 설명할지
                .signWith(key, SignatureAlgorithm.HS256)
                //실제 계산 + 문자열 생성 (최종 JWT)
                .compact();
    }

    //토큰의 subject(email 등)
    public String getSubject(String token) {
        //전달된 토큰 검증 + 파싱해서 클레임 반환. 표준 클레임 sub 으로 꺼내서 반환
        //sub: 이 토큰을 누구에게 발급했는가 (보통 이메일 or userId 문자열)
        return parseAllClaims(token).getSubject();
    }

    // 전달된 토큰이 정상적인 액세스 토큰인지 판정
    public boolean validateAccessToken(String token) {
        try {
            //파싱 성공 -> true
            parseAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //userId 클레임(Long/Number/String 허용)
    //userId가 여러타입으로 넘어올 수 있기 때문에 그 모든 경우를 Long 으로 정규회해서 일관된 타입을 보장하려는 의도
    public Long getUserId(String token) {
        Claims c = parseAllClaims(token);
        Object id = c.get("userId");
        if (id == null) return null;
        if (id instanceof Long l) return l;
        if (id instanceof Number n) return n.longValue();
        if (id instanceof String s) {
            //값이 숫자 문자열이면 Long 으로 파싱 시도
            //아니면 예외가 나므로 조용히 무시하고 아래로
            try { return Long.valueOf(s); } catch (NumberFormatException ignore) {}
        }
        //위 경우에 해당 없을 시 null 반환
        return null;
    }

    // JwtTokenProvider.java — parseAllClaims 완화
    public Claims parseAllClaims(String token) {
        // 파서 빌더를 연다.
        return Jwts.parserBuilder()
                // 서명 검증에 사용할 키 지정 (이 키로 서명 무결성 검증)
                .setSigningKey(key)
                // 발급자 검증
                // iss 클레임이 반드시 issuer와 일치해야함.
                .requireIssuer(issuer)
                // 설정 반영해 인스턴스 생성
                .build()
                // 서명된 JWT를 파싱 + 검증
                .parseClaimsJws(token)
                //파싱된 페이로드를 Claims로 반환
                .getBody();
    }



}
