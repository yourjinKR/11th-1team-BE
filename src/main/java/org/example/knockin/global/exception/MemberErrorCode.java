package org.example.knockin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(1000, HttpStatus.NOT_FOUND, "회원 조회에 실패하였습니다."),
    DECLARATION_DUPLICATE(1001, HttpStatus.BAD_REQUEST, "이미 신고한 회원입니다."),
    BASIC_INFO_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "기본 정보 조회에 실패하였습니다"),
    ;

    private final Integer no;
    private final HttpStatus httpStatus;
    private final String message;
}
