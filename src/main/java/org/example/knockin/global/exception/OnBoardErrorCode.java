package org.example.knockin.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OnBoardErrorCode implements ErrorCode{
    ONBOARD_BASIC_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "기본정보 저장을 하지 못하였습니다."),
    ONBOARD_TERM_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "약관정보 저장을 하지 못하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
