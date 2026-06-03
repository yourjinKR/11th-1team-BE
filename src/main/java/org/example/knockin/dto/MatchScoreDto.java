package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class MatchScoreDto {
    @Data
    @Schema(name = "MatchScoreRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MatchScoreResponse")
    public static class Response {
        private Compatibility compatibility;

        @Data
        @Schema(name = "MatchScoreCompatibility")
        public static class Compatibility {
            private Integer score;
            private List<LifeStyleInfo> lifeStyleInfo;

            @Data
            @Schema(name = "MatchScoreCompatibilityLifeStyleInfo")
            public static class LifeStyleInfo {
                private String title;
                private String percent;
            }
        }
    }
}
