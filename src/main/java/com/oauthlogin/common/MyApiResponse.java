package com.oauthlogin.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class MyApiResponse<T> {

    private final static int SUCCESS = 200;
    private final static int NOT_FOUND = 400;
    private final static int FAILED = 500;
    private final static String SUCCESS_MESSAGE = "SUCCESS";
    private final static String NOT_FOUND_MESSAGE = "NOT FOUND";
    private final static String FAILED_MESSAGE = "서버에서 오류가 발생하였습니다.";
    private final static String INVALID_ACCESS_TOKEN = "Invalid access token.";
    private final static String INVALID_REFRESH_TOKEN = "Invalid refresh token.";
    private final static String NOT_EXPIRED_TOKEN_YET = "Not expired token yet.";

    private final ApiResponseHeader header;
    private final Map<String, T> body;

    public static <T> MyApiResponse<T> success(String name, T body) {
        Map<String, T> map = new HashMap<>();
        map.put(name, body);

        return new MyApiResponse(new ApiResponseHeader(SUCCESS, SUCCESS_MESSAGE), map);
    }

    public static <T> MyApiResponse<T> fail() {
        return new MyApiResponse(new ApiResponseHeader(FAILED, FAILED_MESSAGE), null);
    }

    public static <T> MyApiResponse<T> invalidAccessToken() {
        return new MyApiResponse(new ApiResponseHeader(FAILED, INVALID_ACCESS_TOKEN), null);
    }

    public static <T> MyApiResponse<T> invalidRefreshToken() {
        return new MyApiResponse(new ApiResponseHeader(FAILED, INVALID_REFRESH_TOKEN), null);
    }

    public static <T> MyApiResponse<T> notExpiredTokenYet() {
        return new MyApiResponse(new ApiResponseHeader(FAILED, NOT_EXPIRED_TOKEN_YET), null);
    }
    
    public static <T> MyApiResponse<T> customMessage(int code, String message) {
        return new MyApiResponse(new ApiResponseHeader(code, message), null);
    }

}
