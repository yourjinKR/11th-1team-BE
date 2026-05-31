package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoTermsListDto {
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
            private LocalDateTime createAt;
        }
    }
}
