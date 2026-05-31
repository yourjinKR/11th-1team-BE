package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmSettingDto {
    @Data
    public static class Request {
        private Long settingId;
        private Boolean enabled;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
