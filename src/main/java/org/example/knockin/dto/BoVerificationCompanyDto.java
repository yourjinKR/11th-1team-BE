package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoVerificationCompanyDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "수정 일시")
        private LocalDateTime updatedAt;
    }
}