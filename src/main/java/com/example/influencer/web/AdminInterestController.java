package com.example.influencer.web;

import com.example.influencer.common.api.ApiResponse;
import com.example.influencer.domain.Interest;
import com.example.influencer.repository.InterestRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/interests")
@RequiredArgsConstructor
public class AdminInterestController {

    private final InterestRepository interestRepository;

    /** 카탈로그에 관심사 추가 (중복은 건너뜀) */
    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> add(@RequestBody @NotEmpty Set<String> names) {
        Set<String> existing = interestRepository.findAll().stream()
                .map(Interest::getName)
                .collect(Collectors.toSet());

        List<String> added = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (String n : names) {
            if (existing.contains(n)) {
                skipped.add(n);
            } else {
                interestRepository.save(Interest.builder().name(n).build());
                added.add(n);
            }
        }

        return ApiResponse.ok(Map.of(
                "added", added,
                "skippedDuplicates", skipped
        ));
    }

    /** 카탈로그에서 관심사 삭제 (없으면 missing에 기록) */
    @DeleteMapping
    @Transactional
    public ApiResponse<Map<String, Object>> remove(@RequestBody @NotEmpty Set<String> names) {
        List<String> removed = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String n : names) {
            var opt = interestRepository.findByName(n);
            if (opt.isPresent()) {
                interestRepository.delete(opt.get()); // user_interests는 FK ON DELETE CASCADE
                removed.add(n);
            } else {
                missing.add(n);
            }
        }

        return ApiResponse.ok(Map.of(
                "removed", removed,
                "missing", missing
        ));
    }
}
