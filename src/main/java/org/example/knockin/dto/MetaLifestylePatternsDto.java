package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class MetaLifestylePatternsDto {
    @Data
    @Schema(name = "MetaLifestylePatternsRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MetaLifestylePatternsResponse")
    public static class Response {
        private List<PatternItem> patterns;

        @Data
        @Schema(name = "MetaLifestylePatternsPatternItem")
        public static class PatternItem {
            private Long id;
            private String name;
            private LifePatternType type;
            private List<DetailItem> details;

            @Data
            @Schema(name = "MetaLifestylePatternsPatternItemDetailItem")
            public static class DetailItem {
                private String values;
                private String description;
            }
        }
    }
}
