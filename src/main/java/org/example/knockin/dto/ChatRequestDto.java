package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRequestDto {
    @Data
    public static class Request {
        private Long requestee;
        private Long boardId;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
