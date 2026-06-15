package org.example.knockin.global.api;

import org.example.knockin.global.exception.ErrorCode;

public record ErrorResponse(
        Integer codeNo,
        String code,
        String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getNo(), errorCode.name(), errorCode.getMessage());
    }
}
