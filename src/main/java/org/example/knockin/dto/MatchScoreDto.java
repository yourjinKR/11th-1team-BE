package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class MatchScoreDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private Compatibility compatibility;

        @Data
        public static class Compatibility {
            private Integer score;
            private List<LifeStyleInfo> lifeStyleInfo;

            @Data
            public static class LifeStyleInfo {
                private String title;
                private String percent;
            }
        }
    }
}
