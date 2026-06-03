package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailConfirmDto {
    @Data
    @Schema(name = "EmailConfirmRequest")
    public static class Request {
        private String email;
        private String authNo;
    }

    @Data
    @Schema(name = "EmailConfirmResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
