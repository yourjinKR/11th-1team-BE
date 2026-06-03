package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveProfileLifeStyleDto {
    @Data
    @Schema(name = "SaveProfileLifeStyleRequest")
    public static class Request {
        private List<Long> lifestyles;
    }

    @Data
    @Schema(name = "SaveProfileLifeStyleResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
