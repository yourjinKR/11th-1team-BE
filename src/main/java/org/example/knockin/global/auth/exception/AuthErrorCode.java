package org.example.knockin.global.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.knockin.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "인증 토큰이 유효하지 않습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
    MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "회원을 찾을수 없습니다."),
    OAUTH_UNLINK_FAIL(HttpStatus.UNAUTHORIZED, "Oauth2 서버에 unlink API호출에 실패했습니다."),
    ILLEGAL_LOGIN_ACCESS(HttpStatus.UNAUTHORIZED, "비정상적인 동작으로 인해 로그인에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
