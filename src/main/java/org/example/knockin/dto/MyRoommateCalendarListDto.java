package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyRoommateCalendarListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<Calendar> calendars;

        @Data
        public static class Calendar {
            private Long calendarId;
            private String writer;
            private LocalDateTime startDt;
            private LocalDateTime endDt;
            private LocalDateTime createAt;
            private String type;
            private String title;
        }
    }
}
