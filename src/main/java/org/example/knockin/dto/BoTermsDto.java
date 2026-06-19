package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoTermsDto {
    @Data
    public static class Request {
        @Schema(description = "제목")
        private String title;
        @Schema(description = "내용")
        private String contents;
    }

    @Data
    public static class Response {
        @Schema(description = "수정 일시")
        private LocalDateTime updatedAt;
    }
}