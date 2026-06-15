package org.example.knockin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoommateBoardErrorCode implements ErrorCode {
    ROOMMATE_BOARD_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "룸메이트 게시글 조회에 실패했습니다.");

    private final Integer no;
    private final HttpStatus httpStatus;
    private final String message;
}
