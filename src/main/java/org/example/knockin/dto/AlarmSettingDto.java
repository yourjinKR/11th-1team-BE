package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmSettingDto {
    @Data
    @Schema(name = "AlarmSettingRequest")
    public static class Request {
        private Long settingId;
        private Boolean enabled;
    }

    @Data
    @Schema(name = "AlarmSettingResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
