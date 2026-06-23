package org.example.knockin.dto;


import java.time.LocalDateTime;

public record ChatRoomLeftEvent(
        Long chatRoomId,
        LocalDateTime leftAt,
        String message
) {
}