package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyPreferencesAllDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private List<ModifyPreferencesLifeStyleDto.Request.LifeStyleInfo> lifestyles;
        private List<Long> conditions;
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
