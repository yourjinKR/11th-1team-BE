package org.example.knockin.global.auth.exception;

import lombok.Getter;
import org.example.knockin.global.exception.ErrorCode;

@Getter
public class AuthException extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
