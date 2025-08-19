package com.example.influencer.w

import com.example.influencer.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping
    public ApiResponse<String> health(){
        return ApiResponse.ok("ok");
    }
}
