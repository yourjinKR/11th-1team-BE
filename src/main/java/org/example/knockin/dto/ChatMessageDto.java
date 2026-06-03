package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    @Data
    @Schema(name = "ChatMessageRequest")
    public static class Request {
        @Schema(description = "메시지")
        private String message;
        @Schema(description = "타입/유형")
        private String type;
    }

    @Data
    @Schema(name = "ChatMessageResponse")
    public static class Response {
        @Schema(description = "메시지")
        private String message;
        @Schema(description = "타입/유형")
        private String type;
        @Schema(description = "발송자")
        private String sender;
        @Schema(description = "날짜 및 시간")
        private LocalDateTime createdAt;
    }
}
