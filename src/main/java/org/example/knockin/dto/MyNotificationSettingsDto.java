package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class MyNotificationSettingsDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<AlarmSettingItem> alarmsSettings;

        @Data
        public static class AlarmSettingItem {
            private Long id;
            private String name;
            private Boolean isEnable;
        }
    }
}
