package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InquiryDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private InquiryDetail inquirie;

        @Data
        public static class InquiryDetail {
            private Long id;
            private String title;
            private String contents;
            private String writer;
            private String status;
            private LocalDateTime createAt;
            private String type;
            private List<Reply> reply;

            @Data
            public static class Reply {
                private Long id;
                private String title;
                private String contents;
                private String writer;
                private LocalDateTime createAt;
            }
        }
    }
}
