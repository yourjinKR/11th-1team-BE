package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class MyRoommateDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "고유 식별 ID")
        private Long userId;
        @Schema(description = "이름")
        private String userName;
        @Schema(description = "적합도")
        private Compatibility compatibility;
        @Schema(description = "선호도 목록")
        private List<Lifestyle> preferences;

        @Data
        public static class Lifestyle {
            @Schema(description = "고유 식별 ID")
            private Long lifestyleId;
            @Schema(description = "이름")
            private String name;
            @Schema(description = "값")
            private String value;
            @Schema(description = "설명")
            private String description;
            @Schema(description = "타입/유형")
            private LifePatternType type;
        }
    }
}
