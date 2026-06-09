package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyPreferencesLifeStyleDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private List<LifeStyleInfo> lifestyles;

        @Data
        public static class LifeStyleInfo {
            private Long id;
            private Long lifestyleId;
        }
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
