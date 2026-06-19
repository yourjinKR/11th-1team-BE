package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;

import java.time.LocalDateTime;

@Data
public class BoLifeStylePatternDto {
    @Data
    public static class Request {
        @Schema(description = "이름")
        private String name;
        @Schema(description = "유형")
        private LifePatternType type;
        @Schema(description = "값")
        private String value;
        @Schema(description = "설명")
        private String description;
    }

    @Data
    public static class Response {
        @Schema(description = "수정 일시")
        private LocalDateTime updatedAt;
    }
}