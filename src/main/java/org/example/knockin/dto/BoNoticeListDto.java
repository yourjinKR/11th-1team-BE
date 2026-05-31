package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoNoticeListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<NoticeItem> notices;

        @Data
        public static class NoticeItem {
            private Long id;
            private String title;
            private String writer;
            private LocalDateTime createAt;
            private String type;
        }
    }
}
