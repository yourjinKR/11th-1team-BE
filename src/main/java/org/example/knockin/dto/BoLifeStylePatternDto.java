package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;

import java.time.LocalDateTime;

@Data
public class BoLifeStylePatternDto {
    @Data
    @Schema(name = "BoLifeStylePatternRequest")
    public static class Request {
        private String name;
        private LifePatternType type;
        private String value;
        private String description;
    }

    @Data
    @Schema(name = "BoLifeStylePatternResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
