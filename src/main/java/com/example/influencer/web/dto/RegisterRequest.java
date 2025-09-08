package com.example.influencer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest (
    @Email @NotBlank String email,
    @NotBlank String password,
    String nickname,
    String gender,
    Integer age
){}
