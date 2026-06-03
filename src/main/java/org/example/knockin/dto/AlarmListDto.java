package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AlarmListDto {
    @Data
    @Schema(name = "AlarmListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "AlarmListResponse")
    public static class Response {
        @Schema(description = "알림 목록")
        private List<Alarm> alarms;

        @Data
        @Schema(name = "AlarmListAlarm")
        public static class Alarm {
            @Schema(description = "제목")
            private String title;
            @Schema(description = "내용")
            private String contents;
            @Schema(description = "읽음 여부")
            private Boolean isRead;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
        }
    }
}
