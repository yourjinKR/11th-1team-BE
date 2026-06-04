package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import lombok.NoArgsConstructor;

@Data
public class BoardDto {
    @Data
    @Schema(name = "BoardSaveRequest")
    public static class Request {
        @Schema(description = "제목")
        private String title;
        @Schema(description = "내용")
        private String contents;
        @Schema(description = "보증금")
        private Integer deposit;
        @Schema(description = "월세")
        private Integer mountlyRent;
        @Schema(description = "관리비")
        private Integer managementCost;
        @Schema(description = "방 타입 ID")
        private Long roomType;
        @Schema(description = "지역 ID")
        private Long region;
        @Schema(description = "입주 가능일")
        private LocalDateTime comeableAt;
        @Schema(description = "이미지 목록")
        private List<ImageDto> images;

        @Data
        @Schema(name = "BoardImageRequest")
        public static class ImageDto {
            @Schema(description = "이미지 URL")
            private String image;
            @Schema(description = "썸네일 여부")
            private Boolean thumbnail;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "BoardSaveRequest")
    public static class Response {
        @Schema(description = "날짜 및 시간")
        private LocalDateTime updatedAt;
    }
}
