package com.example.influencer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                // user_id 컬럼에 인덱스 생성 -> 특정 사용자 토큰 빠른 조회.
                @Index(name = "idx_rt_user", columnList = "user_id"),
                // expires_at 컬럼 인덱스 -> 만료 토큰 정리 시 성능 향상
                @Index(name = "idx_rt_expires", columnList = "expires_at")
        },
        uniqueConstraints = {
                // token 컬럼에 유니크 제약 -> 중복 토큰 방지.
                @UniqueConstraint(name = "uk_rt_token", columnNames = "token")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rt_user"))
    private User user;

    // 실제 리프레시 토큰 값
    @Column(nullable = false, length = 255)
    private String token;

    // 생성 시각
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 만료 시각
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // 취소(로그아웃 등)
    @Column(nullable = false)
    // 기본 생성자가 아닌 빌더 패턴으로 객체 생성 가능
    // @Builder은 기본값을 무시함 -> 빌더로 만들때도 false 값이 자동으로 들어가도록 보장
    @Builder.Default
    private boolean revoked = false;

    //토큰 만료 여부 확인
    public boolean isExpired() {
        //현재 시간이 expiresAt을 지난 경우 -> true 반환
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
