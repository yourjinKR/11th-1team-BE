package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoTermsListDto {
    @Data
    @Schema(name = "BoTermsListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoTermsListResponse")
    public static class Response {
        private List<TermsItem> terms;

        @Data
        @Schema(name = "BoTermsListTermsItem")
        public static class TermsItem {
            private Long id;
            private String title;
            private LocalDateTime createAt;
        }
    }
}
