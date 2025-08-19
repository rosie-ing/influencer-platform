package com.example.influencer.common.api;

import lombok.*;

@Getter @AllArgsConstructor @NoArgsConstructor @Builder
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data){
        return ApiResponse.<T>builder()
                .success(true).code("OK").message("success").data(data).build();
    }

    public static ApiResponse<Void> ok(){
        return ok(null);
    }

    public static ApiResponse<Void> error(String code, String message){
        return ApiResponse.<Void>builder()
                .success(false).code(code).message(message).data(null).build();
    }
}
