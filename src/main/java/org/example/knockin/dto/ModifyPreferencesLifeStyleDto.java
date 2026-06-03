package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyPreferencesLifeStyleDto {
    @Data
    @Schema(name = "ModifyPreferencesLifeStyleRequest")
    public static class Request {
        private List<Long> lifestyles;
    }

    @Data
    @Schema(name = "ModifyPreferencesLifeStyleResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
