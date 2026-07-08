package org.example.knockin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LifePatternErrorCode implements ErrorCode {
    LIFE_PATTERN_NOT_FOUNT(16000, HttpStatus.INTERNAL_SERVER_ERROR, "생활 패턴을 조회하지 못하였습니다.");

    private final Integer no;
    private final HttpStatus httpStatus;
    private final String message;
}
