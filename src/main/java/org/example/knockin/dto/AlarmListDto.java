package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AlarmListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<Alarm> alarms;

        @Data
        public static class Alarm {
            private String title;
            private String contents;
            private Boolean isRead;
            private LocalDateTime createAt;
        }
    }
}
