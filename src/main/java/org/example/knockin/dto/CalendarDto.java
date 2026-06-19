package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CalendarDto {
    @Data
    public static class Request {
        @Schema(description = "roommate id")
        private Long roommateId;
        @Schema(description = "제목")
        private String title;
        @Schema(description = "내용")
        private String contents;
        @Schema(description = "start dt")
        private LocalDateTime startDt;
        @Schema(description = "end dt")
        private LocalDateTime endDt;
    }

    @Data
    public static class Response {
        @Schema(description = "수정 일시")
        private LocalDateTime updatedAt;
    }
}