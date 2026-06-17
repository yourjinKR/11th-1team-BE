package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class AlarmSettingDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long settingId;
        private Boolean enabled;
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
