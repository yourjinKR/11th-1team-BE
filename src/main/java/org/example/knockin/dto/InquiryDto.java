package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class InquiryDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long categoryId;
        private String title;
        private String contents;
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
