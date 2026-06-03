package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SavePreferencesConditionsDto {
    @Data
    @Schema(name = "SavePreferencesConditionsRequest")
    public static class Request {
        private List<Long> conditions;
    }

    @Data
    @Schema(name = "SavePreferencesConditionsResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
