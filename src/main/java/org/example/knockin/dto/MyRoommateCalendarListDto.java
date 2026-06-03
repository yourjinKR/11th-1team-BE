package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyRoommateCalendarListDto {
    @Data
    @Schema(name = "MyRoommateCalendarListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MyRoommateCalendarListResponse")
    public static class Response {
        @Schema(description = "캘린더 목록")
        private List<Calendar> calendars;

        @Data
        @Schema(name = "MyRoommateCalendarListCalendar")
        public static class Calendar {
            @Schema(description = "고유 식별 ID")
            private Long calendarId;
            @Schema(description = "작성자")
            private String writer;
            @Schema(description = "시작일")
            private LocalDateTime startDt;
            @Schema(description = "종료일")
            private LocalDateTime endDt;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
            @Schema(description = "타입/유형")
            private String type;
            @Schema(description = "제목")
            private String title;
        }
    }
}
