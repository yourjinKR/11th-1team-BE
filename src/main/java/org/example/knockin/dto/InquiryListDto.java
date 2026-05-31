package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InquiryListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<InquiryItem> inquiries;

        @Data
        public static class InquiryItem {
            private Long id;
            private String title;
            private String writer;
            private String status;
            private LocalDateTime createAt;
            private String type;
        }
    }
}
