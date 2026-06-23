package org.example.knockin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoommateMatchingRequiredErrorCode implements ErrorCode {
    NOT_FOUND(17000, HttpStatus.NOT_FOUND, "룸메이트 매칭 요청 조회에 실패하였습니다."),
    DUPLICATE(17001, HttpStatus.NOT_FOUND, "이미 대기중인 룸메이트 요청이 존재합니다."),
    ;

    private final Integer no;
    private final HttpStatus httpStatus;
    private final String message;
}
