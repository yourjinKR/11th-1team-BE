package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoTermsDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<TermsItem> terms;

        @Data
        public static class TermsItem {
            private Long id;
            private String title;
            private String contents;
            private LocalDateTime createAt;
        }
    }
}
