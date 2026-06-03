package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoNoticeDetailDto {
    @Data
    @Schema(name = "BoNoticeDetailRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoNoticeDetailResponse")
    public static class Response {
        private NoticeDetail notice;

        @Data
        @Schema(name = "BoNoticeDetailNoticeDetail")
        public static class NoticeDetail {
            private Long id;
            private String title;
            private String contents;
            private String writer;
            private LocalDateTime createAt;
            private String type;
        }
    }
}
