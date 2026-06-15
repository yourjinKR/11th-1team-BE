package org.example.knockin.dto;

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
        private List<PatternItem> patterns;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PatternItem {
            private Long id;
            private String name;
            private LifePatternType type;
            private List<DetailItem> details;

            @Data
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public static class DetailItem {
                private String values;
                private String description;
            }
        }
    }
}
