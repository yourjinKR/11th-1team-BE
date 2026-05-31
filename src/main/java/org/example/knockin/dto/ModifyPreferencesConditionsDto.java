package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyPreferencesConditionsDto {
    @Data
    public static class Request {
        private List<Long> conditions;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
