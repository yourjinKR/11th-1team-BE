package org.example.knockin.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.knockin.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    INVALID_PROVIDER_TOKEN(HttpStatus.UNAUTHORIZED, "소셜 인증 토큰이 유효하지 않습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 제공자입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
