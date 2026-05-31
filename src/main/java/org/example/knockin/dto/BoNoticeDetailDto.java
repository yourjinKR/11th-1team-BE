package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoNoticeDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private NoticeDetail notice;

        @Data
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
