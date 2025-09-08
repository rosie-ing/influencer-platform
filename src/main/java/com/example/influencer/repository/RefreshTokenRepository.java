package com.example.influencer.repository;

import com.example.influencer.domain.RefreshToken;
import com.example.influencer.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //클라이언트가 보낸 리프레시 토큰 검증용
    Optional<RefreshToken> findByToken(String token);

    //토큰 로테이션/ 로그아웃 시 현재 유효 토큰들 조회
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    //강제 로그아웃(모든 기기) 같은 용도
    long deleteByUser(User user);

    //만료 토큰 정리 배치용
    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}
