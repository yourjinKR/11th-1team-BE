package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDto {
    @Data
    @Schema(name = "ReportRequest")
    public static class Request {
        private String contents;
    }

    @Data
    @Schema(name = "ReportResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
