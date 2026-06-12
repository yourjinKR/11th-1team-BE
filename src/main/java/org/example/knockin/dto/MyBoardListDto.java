package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyBoardListDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private List<BoardItem> boards;

        @Data
        @Builder
        public static class BoardItem {
            @Schema(description = "게시판 고유 식별 ID")
            private Long boardId;
            @Schema(description = "썸네일 이미지 URL")
            private String image;
            @Schema(description = "게시물 제목")
            private String title;
            @Schema(description = "보증금")
            private Integer deposit;
            @Schema(description = "월세")
            private Integer monthlyRent;
            @Schema(description = "지역")
            private String region;
            @Schema(description = "게시물 생성일")
            private LocalDateTime createdAt;
            @Schema(description = "작성자")
            private String memberName;
            @Schema(description = "방 유형")
            private String roomTypes;
        }
    }
}
