package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoInquiryReplyDto {
    @Data
    public static class Request {
        private Long inquirieId;
        private String contents;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
