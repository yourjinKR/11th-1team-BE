package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.chat.ChattingRequiredStatus;
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
        @Schema(description = "요청자 정보")
        private RequesterInfo requester;
        @Schema(description = "수신자 정보")
        private RequesteeInfo requestee;

        @Data
        public static class RequesterInfo {
            @Schema(description = "이름")
            private String name;
            @Schema(description = "라이프스타일 목록")
            private List<Lifestyle> lifeStyles;
            @Schema(description = "점수")
            private Integer score;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
        }

        @Data
        public static class RequesteeInfo {
            @Schema(description = "이름")
            private String name;
            @Schema(description = "라이프스타일 목록")
            private List<Lifestyle> lifeStyles;
            @Schema(description = "점수")
            private Integer score;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
            @Schema(description = "채팅 요청 상태")
            private ChattingRequiredStatus status;
        }

        @Data
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
    }
}
