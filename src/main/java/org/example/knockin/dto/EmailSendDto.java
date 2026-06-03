package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailSendDto {
    @Data
    @Schema(name = "EmailSendRequest")
    public static class Request {
        private String email;
    }

    @Data
    @Schema(name = "EmailSendResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
