package com.example.influencer.config;

import com.example.influencer.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

//local 프로필에서만 활성화
@Profile("local")
// log 자동 생성
@Slf4j
//스케줄 잡
@Configuration
@EnableScheduling
//final 필드를 인자로 받는 생성자 자동생성
@RequiredArgsConstructor
public class RefreshTokenCleanupConfig {

    private final RefreshTokenRepository refreshTokenRepository;

    // 스케줄: 10초마다 실행 (테스트용)
    // 만료된 리프레시 토큰을 매시간 정리 (0분마다)
    @Scheduled(cron = "*/10 * * * * *")
    public void cleanupExpired() {
        //매 실행 시점의 LocalDateTime.now()보다 과거인 토큰들을 deleteByExpiresAtBefore(...)로 일관 삭제
        long deleted = refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        //삭제 건수 있다면 로그 남김.
        if (deleted > 0) {
            log.info("[RefreshTokenCleanup] deleted {} expired refresh tokens", deleted);
        }
    }
}
