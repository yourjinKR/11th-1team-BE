package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.knockin.dto.BoardDetailDto.Response.Condition;
import org.example.knockin.dto.BoardDetailDto.Response.ConditionWeight;
import org.example.knockin.dto.BoardDetailDto.Response.Lifestyle;

public class BoardEditDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @Valid
        @Size(max = 10)
        @Schema(description = "이미지 목록")
        private List<BoardDetailDto.Response.FileDetailDto> images;

        @NotNull
        @Schema(description = "제목")
        private String title;

        @NotNull
        @Schema(description = "보증금")
        private int deposit;

        @NotNull
        @Schema(description = "월세")
        private int monthlyRent;

        @NotNull
        @Schema(description = "관리비")
        private int managementCost;

        @NotNull
        @Schema(description = "룸 형태")
        private RoomTypeInfo roomType;

        @NotNull
        @Schema(description = "지역")
        private RegionInfo region;

        @Schema(description = "입주 가능시기")
        private LocalDateTime comeableAt;

        @Schema(description = "방 추가 옵션 목록")
        private List<BoardOptionInfo> roomExtraOptions;

        @NotNull
        @Schema(description = "내용")
        private String contents;

        @Schema(description = "생활패턴")
        private List<Lifestyle> lifeStyles;

        @Schema(description = "선호 룸메이트 조건 목록")
        private List<Condition> conditions;

        @Schema(description = "선호 룸메이트 중요 조건 목록")
        private List<ConditionWeight> conditionWeights;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RoomTypeInfo {
            @Schema(description = "고유 식별 ID")
            private Long roomTypeId;

            @Schema(description = "이름")
            private String name;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RegionInfo {
            @Schema(description = "고유 식별 ID")
            private Long regionId;

            @Schema(description = "지역명")
            private String fullName;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BoardOptionInfo {
            @Schema(description = "고유 식별 ID")
            private Long extraOptionId;

            @Schema(description = "옵션명")
            private String name;
        }
    }
}
