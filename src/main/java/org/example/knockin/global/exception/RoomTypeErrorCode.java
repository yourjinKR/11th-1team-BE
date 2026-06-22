package org.example.knockin.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RoomTypeErrorCode implements ErrorCode {
    ROOM_TYPE_NOT_FOUNT(15000, HttpStatus.INTERNAL_SERVER_ERROR, "약관 정보를 조회하지 못하였습니다.");

    private final Integer no;
    private final HttpStatus httpStatus;
    private final String message;
}
