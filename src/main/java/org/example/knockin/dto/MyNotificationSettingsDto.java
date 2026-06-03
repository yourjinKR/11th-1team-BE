package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class MyNotificationSettingsDto {
    @Data
    @Schema(name = "MyNotificationSettingsRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MyNotificationSettingsResponse")
    public static class Response {
        private List<AlarmSettingItem> alarmsSettings;

        @Data
        @Schema(name = "MyNotificationSettingsAlarmSettingItem")
        public static class AlarmSettingItem {
            private Long id;
            private String name;
            private Boolean isEnable;
        }
    }
}
