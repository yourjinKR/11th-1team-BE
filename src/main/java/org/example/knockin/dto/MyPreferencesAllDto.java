package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.util.List;

@Data
public class MyPreferencesAllDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<Lifestyle> lifestyles;
        private List<Condition> conditions;

        @Data
        public static class Lifestyle {
            private Long lifestyleId;
            private String name;
            private String value;
            private String description;
            private LifePatternType type;
        }

        @Data
        public static class Condition {
            private Long conditionsId;
            private String name;
        }
    }
}
