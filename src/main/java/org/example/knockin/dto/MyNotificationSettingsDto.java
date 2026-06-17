package org.example.knockin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class MyNotificationSettingsDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private List<AlarmSettingItem> alarmsSettings;

        @Data
        @Builder
        public static class AlarmSettingItem {
            private Long id;
            private String name;
            private Boolean isEnable;
        }
    }
}
