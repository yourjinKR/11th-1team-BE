package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoardDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private Long boardId;
        private List<String> images;
        private String title;
        private Integer deposit;
        private Integer mounthRent;
        private Long roomType;
        private Long region;
        private LocalDateTime createAt;
        private Long viewer;
        private String contents;
        private List<Long> roomOption;
        private List<Lifestyle> lifeStyles;
        private List<Preference> preferences;
        private List<Condition> conditions;
        private String writer;
        private Boolean isAuthStudent;
        private Boolean isAuthEmployee;
        private Compatibility compatibility;

        @Data
        public static class Lifestyle {
            private Long lifestyleId;
            private String name;
            private String value;
            private String description;
            private LifePatternType type;
        }

        @Data
        public static class Preference {
            private Long preferencesId;
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
