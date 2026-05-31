package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class BoLifeStylePatternListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<PatternItem> patterns;

        @Data
        public static class PatternItem {
            private Long id;
            private String name;
            private LifePatternType type;
            private List<DetailItem> details;

            @Data
            public static class DetailItem {
                private String values;
                private String description;
            }
        }
    }
}
