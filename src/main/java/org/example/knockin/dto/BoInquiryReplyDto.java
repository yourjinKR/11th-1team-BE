package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoInquiryReplyDto {
    @Data
    @Schema(name = "BoInquiryReplyRequest")
    public static class Request {
        private Long inquirieId;
        private String contents;
    }

    @Data
    @Schema(name = "BoInquiryReplyResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
