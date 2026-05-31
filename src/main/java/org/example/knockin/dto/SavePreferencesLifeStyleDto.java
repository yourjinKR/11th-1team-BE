package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SavePreferencesLifeStyleDto {
    @Data
    public static class Request {
        private List<Long> lifestyles;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
