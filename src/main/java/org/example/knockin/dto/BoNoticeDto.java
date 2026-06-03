package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoNoticeDto {
    @Data
    @Schema(name = "BoNoticeRequest")
    public static class Request {
        private String title;
        private String contents;
    }

    @Data
    @Schema(name = "BoNoticeResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
