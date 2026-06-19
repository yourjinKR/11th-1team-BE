package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatRoomLeftEvent(
        Long memberId,
        Long chatRoomId,
        LocalDateTime leftAt
) {
}