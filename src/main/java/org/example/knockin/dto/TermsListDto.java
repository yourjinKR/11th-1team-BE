package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class TermsListDto {
    @Data
    @Schema(name = "TermsListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "TermsListResponse")
    public static class Response {
        private List<TermsItem> terms;

        @Data
        @Schema(name = "TermsListTermsItem")
        public static class TermsItem {
            private Long id;
            private String title;
        }
    }
}
