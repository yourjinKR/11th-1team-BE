package org.example.knockin.dto;

import java.time.LocalDateTime;

public record ChatRoomLeftEvent(
        Long memberId,
        Long chatRoomId,
        LocalDateTime leftAt
) {
}
