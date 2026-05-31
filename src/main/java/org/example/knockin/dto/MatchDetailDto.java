package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.room.RoomProfileType;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MatchDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private Integer minDeposit;
        private Integer maxDeposit;
        private Integer deposit;
        private Integer minMounthRent;
        private Integer maxMounthRent;
        private Integer mounthRent;
        private RoomProfileType roomProfileType;
        private Long region;
        private List<Long> roomOption;
        private LocalDateTime comeableAt;
        private List<Lifestyle> lifeStyles;
        private List<Preference> preferences;
        private List<Condition> conditions;
        private String name;
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
