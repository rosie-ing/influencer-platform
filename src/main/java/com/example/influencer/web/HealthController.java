package com.example.influencer.web;

import com.example.influencer.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate; // DataSource 기반으로 자동 주입됨

    @GetMapping
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("ok"));
    }

    @GetMapping("/db")
    public ResponseEntity<ApiResponse<String>> db() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return ResponseEntity.ok(ApiResponse.ok(one != null && one == 1 ? "ok" : "fail"));
    }
}
