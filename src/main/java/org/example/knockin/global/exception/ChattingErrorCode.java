package org.example.knockin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChattingErrorCode implements ErrorCode{
    ROOM_NOT_FOUND(9000, HttpStatus.NOT_FOUND, "채팅방 조회에 실패하였습니다."),
    ROOM_MEMBER_NOT_FOUND(9001, HttpStatus.NOT_FOUND, "채팅방 멤버 조회에 실패하였습니다."),
    REQUIRED_NOT_FOUND(9002, HttpStatus.NOT_FOUND, "채팅요청 조회에 실패하였습니다."),
    REQUIRED_DUPLICATE(9003, HttpStatus.BAD_REQUEST, "이미 초대한 채팅요청이 있습니다."),
    ROOM_CAPACITY_EXCEEDED(9004, HttpStatus.BAD_REQUEST, "채팅방 최대 인원이 초과되었습니다."),
    MESSAGE_PAYLOAD_INVALID(9005, HttpStatus.BAD_REQUEST, "채팅 메시지 요청 형식이 올바르지 않습니다."),
    ;

    private final Integer no;
    private final HttpStatus httpStatus;
    private final String message;
}
