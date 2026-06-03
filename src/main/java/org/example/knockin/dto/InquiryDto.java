package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InquiryDto {
    @Data
    @Schema(name = "InquiryRequest")
    public static class Request {
        private Long categoryId;
        private String title;
        private String contents;
    }

    @Data
    @Schema(name = "InquiryResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
