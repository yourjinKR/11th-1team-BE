package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SavePreferencesLifeStyleDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private List<Long> lifestyles;
    }

    @Data
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
