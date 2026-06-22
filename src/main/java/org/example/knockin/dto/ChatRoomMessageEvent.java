package org.example.knockin.dto;

import lombok.Builder;

@Builder
public record ChatRoomMessageEvent(
        Long chatRoomId,
        Long senderId,
        String clientMessageId,
        MessageType messageType,
        String message,
        String imageUrl
) {
}
