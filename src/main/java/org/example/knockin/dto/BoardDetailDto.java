package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.life.LifePatternType;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoardDetailDto {
    @Data
    @Schema(name = "BoardDetailRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoardDetailResponse")
    public static class Response {
        @Schema(description = "고유 식별 ID")
        private Long boardId;
        @Schema(description = "이미지 목록")
        private List<String> images;
        @Schema(description = "제목")
        private String title;
        @Schema(description = "보증금")
        private Integer deposit;
        @Schema(description = "월세")
        private Integer mounthRent;
        @Schema(description = "방 타입 ID")
        private Long roomType;
        @Schema(description = "지역 ID")
        private Long region;
        @Schema(description = "날짜 및 시간")
        private LocalDateTime createAt;
        @Schema(description = "조회수")
        private Long viewer;
        @Schema(description = "내용")
        private String contents;
        @Schema(description = "방 옵션 목록")
        private List<Long> roomOption;
        @Schema(description = "라이프스타일 목록")
        private List<Lifestyle> lifeStyles;
        @Schema(description = "선호도 목록")
        private List<Preference> preferences;
        @Schema(description = "조건 목록")
        private List<Condition> conditions;
        @Schema(description = "작성자")
        private String writer;
        @Schema(description = "학생 인증 여부")
        private Boolean isAuthStudent;
        @Schema(description = "직장인 인증 여부")
        private Boolean isAuthEmployee;
        @Schema(description = "적합도")
        private Compatibility compatibility;

        @Data
        @Schema(name = "BoardDetailLifestyle")
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

        @Data
        @Schema(name = "BoardDetailPreference")
        public static class Preference {
            @Schema(description = "고유 식별 ID")
            private Long preferencesId;
            @Schema(description = "이름")
            private String name;
            @Schema(description = "값")
            private String value;
            @Schema(description = "설명")
            private String description;
            @Schema(description = "타입/유형")
            private LifePatternType type;
        }

        @Data
        @Schema(name = "BoardDetailCondition")
        public static class Condition {
            @Schema(description = "고유 식별 ID")
            private Long conditionsId;
            @Schema(description = "이름")
            private String name;
        }

        @Data
        @Schema(name = "BoardDetailCompatibility")
        public static class Compatibility {
            @Schema(description = "점수")
            private Integer score;
            @Schema(description = "라이프스타일 정보 목록")
            private List<LifeStyleInfo> lifeStyleInfo;

            @Data
            @Schema(name = "BoardDetailCompatibilityLifeStyleInfo")
            public static class LifeStyleInfo {
                @Schema(description = "제목")
                private String title;
                @Schema(description = "백분율")
                private String percent;
            }
        }
    }
}
