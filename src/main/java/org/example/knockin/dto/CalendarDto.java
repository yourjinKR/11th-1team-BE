package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CalendarDto {
    @Data
    @Schema(name = "CalendarRequest")
    public static class Request {
        private Long roommateId;
        private String title;
        private String contents;
        private LocalDateTime startDt;
        private LocalDateTime endDt;
    }

    @Data
    @Schema(name = "CalendarResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
