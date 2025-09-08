package com.example.influencer.repository;

import com.example.influencer.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //이메일 중복 체크
    boolean existsByEmail(String email);

    //이메일 기준으로 유저 조회
    Optional<User> findByEmail(String email);

    //닉네임 중복 체크
    boolean existsByNickname(String nickname);


}
