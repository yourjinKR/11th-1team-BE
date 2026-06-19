package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoNoticeDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "공지사항")
        private NoticeDetail notice;

        @Data
        public static class NoticeDetail {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "제목")
            private String title;
            @Schema(description = "내용")
            private String contents;
            @Schema(description = "작성자")
            private String writer;
            @Schema(description = "생성 일시")
            private LocalDateTime createAt;
            @Schema(description = "유형")
            private String type;
        }
    }
}