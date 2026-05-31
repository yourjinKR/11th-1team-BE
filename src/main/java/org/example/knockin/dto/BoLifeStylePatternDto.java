package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;

import java.time.LocalDateTime;

@Data
public class BoLifeStylePatternDto {
    @Data
    public static class Request {
        private String name;
        private LifePatternType type;
        private String value;
        private String description;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
