package com.example.influencer.web;

import com.example.influencer.common.api.ApiResponse;
import com.example.influencer.domain.Interest;
import com.example.influencer.domain.User;
import com.example.influencer.repository.InterestRepository;
import com.example.influencer.service.UserService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestRepository interestRepository;
    private final UserService userService;

    /** 관심사 전체 이름 목록 */
    @GetMapping
    public ApiResponse<Set<String>> list() {
        Set<String> names = interestRepository.findAll()
                .stream()
                .map(Interest::getName)
                .collect(Collectors.toSet());
        return ApiResponse.ok(names);
    }

    /** 내 관심사 저장/갱신 */
    @PostMapping("/me")
    @Transactional
    public ApiResponse<Map<String, Object>> updateMyInterests(
            Authentication authentication,
            @RequestBody @NotEmpty Set<String> interestNames
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("인증 토큰이 없습니다.");
        }

        String email = authentication.getPrincipal().toString();
        User user = userService.getByEmail(email);

        // 저장/갱신만 처리
        userService.updateInterests(user, interestNames);

        // 요청값 그대로 반환 (LAZY 컬렉션 접근 안 함)
        return ApiResponse.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "updatedInterests", interestNames
        ));
    }

    /** 내 관심사 조회 (내가 선택한 것만) */
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ApiResponse<Set<String>> myInterests(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("인증 토큰이 없습니다.");
        }
        String email = authentication.getPrincipal().toString();
        User u = userService.getByEmail(email);

        Set<String> names = u.getInterests().stream()
                .map(Interest::getName)
                .collect(Collectors.toSet());

        return ApiResponse.ok(names);
    }

    /** 내 관심사 일부 삭제 */
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    @DeleteMapping("/me")
    @Transactional
    public ApiResponse<Map<String, Object>> removeMyInterests(
            Authentication authentication,
            @RequestBody @NotEmpty Set<String> interestNames
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("인증 토큰이 없습니다.");
        }
        String email = authentication.getPrincipal().toString();
        User user = userService.getByEmail(email);

        userService.removeInterests(user, interestNames);

        return ApiResponse.ok(Map.of("removed", interestNames));
    }

}
