package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRequestDto {
    @Data
    @Schema(name = "ChatRequestRequest")
    public static class Request {
        private Long requestee;
        private Long boardId;
    }

    @Data
    @Schema(name = "ChatRequestResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
