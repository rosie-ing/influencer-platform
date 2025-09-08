package com.example.influencer.common.api;

import lombok.*;

@Getter @AllArgsConstructor @NoArgsConstructor @Builder
public class ApiResponse<T> {
    private boolean success; //성공 여부
    private String code;  //결과 코드
    private String message; //결과 메시지
    private T data; //응답 본문 데이터 (제네릭 타입 T)

    //성공 응답 with data
    public static <T> ApiResponse<T> ok(T data){
        return ApiResponse.<T>builder()
                .success(true)
                .code("OK")
                .message("success")
                .data(data)
                .build();
    }

    //성공 응답 without data
    public static ApiResponse<Void> ok(){
        return ok(null);
    }

    //에러 응답
    public static ApiResponse<Void> error(String code, String message){
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
