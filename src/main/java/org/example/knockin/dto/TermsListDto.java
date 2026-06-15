package org.example.knockin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class TermsListDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private List<TermsItem> terms;

        @Data
        @Builder
        public static class TermsItem {
            private Long id;
            private String title;
        }
    }
}
