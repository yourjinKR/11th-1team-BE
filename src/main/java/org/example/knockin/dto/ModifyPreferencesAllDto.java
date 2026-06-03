package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModifyPreferencesAllDto {
    @Data
    @Schema(name = "ModifyPreferencesAllRequest")
    public static class Request {
        private List<Long> lifestyles;
        private List<Long> conditions;
    }

    @Data
    @Schema(name = "ModifyPreferencesAllResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
