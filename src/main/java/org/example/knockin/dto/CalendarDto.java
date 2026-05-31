package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CalendarDto {
    @Data
    public static class Request {
        private Long roommateId;
        private String title;
        private String contents;
        private LocalDateTime startDt;
        private LocalDateTime endDt;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
