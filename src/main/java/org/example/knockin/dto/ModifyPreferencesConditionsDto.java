package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyPreferencesConditionsDto {
    @Data
    @Schema(name = "ModifyPreferencesConditionsRequest")
    public static class Request {
        private List<Long> conditions;
    }

    @Data
    @Schema(name = "ModifyPreferencesConditionsResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
