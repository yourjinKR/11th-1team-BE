package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatMessageDto {
    @Data
    public static class Request {
        @Schema(description = "FE 로컬 메시지 식별 ID")
        private String clientMessageId;

        @Schema(description = "채팅 메시지 콘텐츠 유형")
        private MessageType type;

        @Schema(description = "텍스트 메시지 본문")
        private String message;

        @Schema(description = "이미지 URL")
        private String imageUrl;
    }

    @Data
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "WebSocket 이벤트 유형")
        private EventType eventType;

        @Schema(description = "채팅방 ID")
        private Long chatRoomId;

        @Schema(description = "FE 로컬 메시지 식별 ID")
        private String clientMessageId;

        @Schema(description = "발송자 회원 ID")
        private Long senderId;

        @Schema(description = "채팅 메시지 콘텐츠 유형")
        private MessageType type;

        @Schema(description = "텍스트 메시지 본문")
        private String message;

        @Schema(description = "이미지 URL")
        private String imageUrl;

        @Schema(description = "서버 broadcast 시각")
        private LocalDateTime createdAt;

        public static Response chatMessage(ChatRoomMessageEvent event) {
            return new Response(
                    EventType.CHAT_MESSAGE,
                    event.chatRoomId(),
                    event.clientMessageId(),
                    event.senderId(),
                    event.messageType(),
                    event.message(),
                    event.imageUrl(),
                    LocalDateTime.now()
            );
        }

        public static Response userLeft(Long chatRoomId, Long senderId, LocalDateTime createdAt) {
            return new Response(
                    EventType.USER_LEFT,
                    chatRoomId,
                    null,
                    senderId,
                    null,
                    null,
                    null,
                    createdAt
            );
        }
    }
}
