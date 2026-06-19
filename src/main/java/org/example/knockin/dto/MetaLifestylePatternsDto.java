package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class MetaLifestylePatternsDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        @Schema(description = "patterns")
        private List<PatternItem> patterns;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PatternItem {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "이름")
            private String name;
            @Schema(description = "유형")
            private LifePatternType type;
            @Schema(description = "details")
            private List<DetailItem> details;

            @Data
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public static class DetailItem {
                @Schema(description = "values")
                private String values;
                @Schema(description = "설명")
                private String description;
            }
        }
    }
}