package com.example.influencer.service;

import com.example.influencer.config.JwtTokenProvider;
import com.example.influencer.domain.RefreshToken;
import com.example.influencer.domain.User;
import com.example.influencer.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;      // ⬅️ 추가

    @Value("${jwt.refresh-token-validity-days:14}")                   // ⬅️ 기본 14일
    private long refreshTokenValidityDays;


    public User register(String email, String password, String nickname, String gender, Integer age) {
        return userService.register(email, password, nickname, gender, age);
    }

    /** 기존: 비번 검증만 */
    public void login(String email, String rawPassword) {
        User user = userService.getByEmail(email);
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());   // ← 필수!
        String access = tokenProvider.createToken(user.getEmail(), claims);

    }

    /** 기존: 액세스 토큰만 발급 (유지) */
    public String loginAndIssueToken(String email, String rawPassword) {
        User user = userService.getByEmail(email);
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return tokenProvider.createToken(
                user.getEmail(),
                Map.of("scope", "access", "role", user.getRole().name())
        );
    }

    /** 신규: 액세스 + 리프레시 토큰 동시 발급 */
    public TokenPair loginAndIssueTokens(String email, String rawPassword) {
        User user = userService.getByEmail(email);
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String access = tokenProvider.createToken(
                user.getEmail(),
                Map.of("scope", "access", "role", user.getRole().name())
        );
        String refresh = issueRefreshToken(user);
        return new TokenPair(access, refresh);
    }

    /** 리프레시 토큰으로 액세스 토큰 재발급 + 리프레시 로테이션 */
    @Transactional
    public TokenPair rotateRefreshToken(String refreshToken) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));
        if (rt.isRevoked() || rt.isExpired()) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었거나 취소되었습니다.");
        }

        User user = rt.getUser();

        // 기존 리프레시 토큰 취소
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        // 새 리프레시 토큰 발급
        String newRefresh = issueRefreshToken(user);

        // 새 액세스 토큰 발급
        String newAccess = tokenProvider.createToken(
                user.getEmail(),
                Map.of("scope", "access", "role", user.getRole().name())
        );

        return new TokenPair(newAccess, newRefresh);
    }

    /** 현재 기기의 리프레시 토큰 1개만 취소 */
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /** 모든 기기에서 로그아웃 (해당 사용자 리프레시 전부 취소/삭제) */
    public void logoutAll(String email) {
        User user = userService.getByEmail(email);
        refreshTokenRepository.deleteByUser(user);
    }

    /* ===================== 내부 유틸 ===================== */

    /** 리프레시 토큰 1개 생성/저장 후 문자열 반환 */
    private String issueRefreshToken(User user) {
        String token = generateSecureToken();
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenValidityDays))
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);
        return token;
    }

    /** URL-safe & 예측 어려운 랜덤 토큰 생성 */
    private String generateSecureToken() {
        byte[] bytes = new byte[64]; // 512비트
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 응답용 페어 */
    public record TokenPair(String accessToken, String refreshToken) {}
}
