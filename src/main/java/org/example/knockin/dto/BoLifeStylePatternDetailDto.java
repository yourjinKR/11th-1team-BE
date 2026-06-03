package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class BoLifeStylePatternDetailDto {
    @Data
    @Schema(name = "BoLifeStylePatternDetailRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoLifeStylePatternDetailResponse")
    public static class Response {
        private Long id;
        private String name;
        private LifePatternType type;
        private List<DetailItem> details;

        @Data
        @Schema(name = "BoLifeStylePatternDetailDetailItem")
        public static class DetailItem {
            private String values;
            private String description;
        }
    }
}
