package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoInquiryReplyDto {
    @Data
    public static class Request {
        @Schema(description = "inquirie id")
        private Long inquirieId;
        @Schema(description = "내용")
        private String contents;
    }

    @Data
    public static class Response {
        @Schema(description = "수정 일시")
        private LocalDateTime updatedAt;
    }
}