package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    @Data
    public static class Request {
        private String message;
        private String type;
    }

    @Data
    public static class Response {
        private String message;
        private String type;
        private String sender;
        private LocalDateTime createdAt;
    }
}
