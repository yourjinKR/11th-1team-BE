package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.room.RoomProfileType;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MatchListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<Match> matches;

        @Data
        public static class Match {
            private Long userId;
            private String name;
            private Boolean isLike;
            private RoomProfileType roomProfileType;
            private Integer deposit;
            private Integer mounthRent;
            private Integer minDeposit;
            private Integer minMounthRent;
            private Integer maxDeposit;
            private Integer maxMounthRent;
            private LocalDateTime comeableAt;
            private List<Long> roomType;
            private Long region;
            private Integer score;
            private List<Lifestyle> lifeStyles;
            private List<Condition> conditions;
        }

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
