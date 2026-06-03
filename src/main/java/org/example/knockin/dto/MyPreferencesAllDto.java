package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class MyPreferencesAllDto {
    @Data
    @Schema(name = "MyPreferencesAllRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MyPreferencesAllResponse")
    public static class Response {
        @Schema(description = "라이프스타일 목록")
        private List<Lifestyle> lifestyles;
        @Schema(description = "조건 목록")
        private List<Condition> conditions;

        @Data
        @Schema(name = "MyPreferencesAllLifestyle")
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

        @Data
        @Schema(name = "MyPreferencesAllCondition")
        public static class Condition {
            @Schema(description = "고유 식별 ID")
            private Long conditionsId;
            @Schema(description = "이름")
            private String name;
        }
    }
}
