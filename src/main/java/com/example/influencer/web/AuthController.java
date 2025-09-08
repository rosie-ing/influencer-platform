package com.example.influencer.web;

import com.example.influencer.common.api.ApiResponse;
import com.example.influencer.domain.User;
import com.example.influencer.repository.UserRepository;
import com.example.influencer.service.AuthService;
import com.example.influencer.service.UserService;
import com.example.influencer.web.dto.LoginRequest;
import com.example.influencer.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;

    /** 회원가입 */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody @Valid RegisterRequest req) {
        User u = authService.register(req.email(), req.password(), req.nickname(), req.gender(), req.age());
        return ApiResponse.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "nickname", u.getNickname()
        ));
    }

    //로그인: 액세스+리프레시 동시 발급
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody @Valid LoginRequest req) {
        var pair = authService.loginAndIssueTokens(req.email(), req.password());
        return ApiResponse.ok(Map.of(
                "accessToken", pair.accessToken(),
                "refreshToken", pair.refreshToken(),
                "tokenType", "Bearer"
        ));
    }

    //리프레시: 새 액세스+리프레시 (로테이션)
    public static record RefreshRequest(@NotBlank String refreshToken) {}
    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refresh(@RequestBody @Valid RefreshRequest req) {
        var pair = authService.rotateRefreshToken(req.refreshToken());
        return ApiResponse.ok(Map.of(
                "accessToken", pair.accessToken(),
                "refreshToken", pair.refreshToken(),
                "tokenType", "Bearer"
        ));
    }

    /** 내 정보 조회 */
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("인증 토큰이 없습니다.");
        }
        String email = (String) authentication.getPrincipal().toString();
        User u = userService.getByEmail(email);
        var interests = u.getInterests().stream().map(i -> i.getName()).collect(Collectors.toSet());

        return ApiResponse.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "nickname", u.getNickname(),
                "gender", u.getGender(),
                "age", u.getAge(),
                "interests", interests
        ));
    }

    /** 내 정보 수정 */
    public static record UpdateProfileRequest(String nickname, String gender, Integer age) {}
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    @PutMapping("/me")
    public ApiResponse<Map<String, Object>> updateMe(
            Authentication authentication,
            @RequestBody UpdateProfileRequest req
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("인증 토큰이 없습니다.");
        }
        String email = (String) authentication.getPrincipal().toString();
        var updated = userService.updateProfile(email, req.nickname(), req.gender(), req.age());

        return ApiResponse.ok(Map.of(
                "id", updated.getId(),
                "email", updated.getEmail(),
                "nickname", updated.getNickname(),
                "gender", updated.getGender(),
                "age", updated.getAge()
        ));
    }

    /** 비밀번호 변경 */
    public static record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank String newPassword
    ) {}
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @RequestBody @Valid ChangePasswordRequest req
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("인증 토큰이 없습니다.");
        }
        String email = (String) authentication.getPrincipal().toString();
        userService.changePassword(email, req.currentPassword(), req.newPassword());
        return ApiResponse.ok();
    }

    /** 이메일 중복 체크 (공개) */
    @GetMapping("/check-email")
    public ApiResponse<Map<String, Object>> checkEmail(@RequestParam @Email String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ApiResponse.ok(Map.of("available", !exists));
    }

    /** 닉네임 중복 체크 (트림 + 대소문자 무시, 공개) */
    @GetMapping("/check-nickname")
    public ApiResponse<Map<String, Object>> checkNickname(@RequestParam @NotBlank String nickname) {
        String n = nickname.trim();
        boolean exists = userRepository.findAll().stream()
                .map(User::getNickname)
                .filter(Objects::nonNull)
                .anyMatch(s -> s.trim().equalsIgnoreCase(n));
        return ApiResponse.ok(Map.of("available", !exists));
    }

    /** 리프레시 토큰 1개만 취소 (현재 기기 로그아웃, 공개) */
    public static record LogoutRequest(@NotBlank String refreshToken) {}
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid LogoutRequest req) {
        authService.logout(req.refreshToken());
        return ApiResponse.ok();
    }

    /** 모든 기기에서 로그아웃 (인증 필요) */
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("인증 토큰이 없습니다.");
        }
        String email = (String) authentication.getPrincipal().toString();
        authService.logoutAll(email);
        return ApiResponse.ok();
    }
}
