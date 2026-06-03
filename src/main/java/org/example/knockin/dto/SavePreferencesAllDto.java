package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SavePreferencesAllDto {
    @Data
    @Schema(name = "SavePreferencesAllRequest")
    public static class Request {
        private List<Long> lifestyles;
        private List<Long> conditions;
    }

    @Data
    @Schema(name = "SavePreferencesAllResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
