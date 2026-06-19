package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.util.List;

@Data
public class MatchScoreDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "compatibility")
        private Compatibility compatibility;

        @Data
        public static class Compatibility {
            @Schema(description = "점수")
            private Integer score;
            @Schema(description = "생활 style info")
            private List<LifeStyleInfo> lifeStyleInfo;

            @Data
            public static class LifeStyleInfo {
                @Schema(description = "제목")
                private String title;
                @Schema(description = "percent")
                private String percent;
            }
        }
    }
}