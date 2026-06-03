package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class BoLifeStylePatternListDto {
    @Data
    @Schema(name = "BoLifeStylePatternListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoLifeStylePatternListResponse")
    public static class Response {
        private List<PatternItem> patterns;

        @Data
        @Schema(name = "BoLifeStylePatternListPatternItem")
        public static class PatternItem {
            private Long id;
            private String name;
            private LifePatternType type;
            private List<DetailItem> details;

            @Data
            @Schema(name = "BoLifeStylePatternListPatternItemDetailItem")
            public static class DetailItem {
                private String values;
                private String description;
            }
        }
    }
}
