package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SavePreferencesLifeStyleDto {
    @Data
    @Schema(name = "SavePreferencesLifeStyleRequest")
    public static class Request {
        private List<Long> lifestyles;
    }

    @Data
    @Schema(name = "SavePreferencesLifeStyleResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
