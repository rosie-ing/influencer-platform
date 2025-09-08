package com.example.influencer.service;

import com.example.influencer.domain.Interest;
import com.example.influencer.domain.Role;
import com.example.influencer.domain.User;
import com.example.influencer.repository.InterestRepository;
import com.example.influencer.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;


    /** 회원가입 */
    @Transactional
    public User register(String email, String rawPassword, String nickname, String gender, Integer age) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        if (!isValidGender(gender))
            throw new IllegalArgumentException("gender는 '남' 또는 '여'만 가능합니다.");
        validateAge(age);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(nickname)
                .gender(gender)
                .age(age)
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    /** 이메일로 사용자 조회 */
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void updateInterests(User user, Set<String> names) {
        // 요청된 이름들 중 DB(카탈로그)에 없는 항목은 거절
        var result = new HashSet<Interest>();
        var missing = new ArrayList<String>();

        for (String n : names) {
            interestRepository.findByName(n)
                    .ifPresentOrElse(result::add, () -> missing.add(n));
        }

        if (!missing.isEmpty()) {
            // GlobalExceptionHandler에서 400(BAD_REQUEST)로 변환됨
            throw new IllegalArgumentException("카탈로그에 없는 관심사: " + String.join(", ", missing));
        }

        // 컬렉션 참조 유지하며 교체 (JPA 권장)
        var current = user.getInterests();
        current.clear();
        current.addAll(result);

        userRepository.save(user);
    }

    @Transactional
    public void removeInterests(User user, Set<String> names) {
        // 요청에 포함된 이름 중 카탈로그에 없는 건 거절
        var missing = new java.util.ArrayList<String>();
        for (String n : names) {
            if (interestRepository.findByName(n).isEmpty()) {
                missing.add(n);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("카탈로그에 없는 관심사: " + String.join(", ", missing));
        }

        // 사용자 관심사 컬렉션에서 해당 이름들만 제거
        var current = user.getInterests();
        current.removeIf(i -> names.contains(i.getName()));
        userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String email, String nickname, String gender, Integer age) {
        User u = getByEmail(email);

        if (!isValidGender(gender)) {
            throw new IllegalArgumentException("gender는 '남' 또는 '여'만 가능합니다.");
        }

        validateAge(age);


        if (nickname != null) u.setNickname(nickname);
        if (gender != null)   u.setGender(gender);
        if (age != null)      u.setAge(age);

        return userRepository.save(u);
    }

    private boolean isValidGender(String gender) {
        if (gender == null) return true;       // null은 허용 (선택 입력)
        String g = gender.trim();
        return "남".equals(g) || "여".equals(g);
    }


    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User u = getByEmail(email);

        if (!passwordEncoder.matches(currentPassword, u.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);


    }

    private void validateAge(Integer age) {
        if (age != null && (age < 0 || age > 120)) {
            throw new IllegalArgumentException("age는 0~120 사이여야 합니다.");
        }
    }




}
