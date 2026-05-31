package org.example.knockin.dto;

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
        private Long userId;
        private String userName;
        private Compatibility compatibility;
        private List<Lifestyle> preferences;

        @Data
        public static class Compatibility {
            private Integer score;
            private List<LifeStyleInfo> lifeStyleInfo;
        }

        @Data
        public static class LifeStyleInfo {
            private String title;
            private String percent;
        }

        @Data
        public static class Lifestyle {
            private Long lifestyleId;
            private String name;
            private String value;
            private String description;
            private LifePatternType type;
        }
    }
}
