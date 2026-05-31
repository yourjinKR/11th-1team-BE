package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class TermsListDto {
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
        }
    }
}
