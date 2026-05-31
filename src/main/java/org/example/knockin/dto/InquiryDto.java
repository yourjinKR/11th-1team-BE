package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InquiryDto {
    @Data
    public static class Request {
        private Long categoryId;
        private String title;
        private String contents;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
