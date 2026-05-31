package org.example.knockin.dto;

import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatRequestDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private RequesterInfo requester;
        private RequesteeInfo requestee;

        @Data
        public static class RequesterInfo {
            private String name;
            private List<Lifestyle> lifeStyles;
            private Integer score;
            private LocalDateTime createAt;
        }

        @Data
        public static class RequesteeInfo {
            private String name;
            private List<Lifestyle> lifeStyles;
            private Integer score;
            private LocalDateTime createAt;
            private Boolean isAgree;
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
