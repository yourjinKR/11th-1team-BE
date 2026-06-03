package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoTermsDetailDto {
    @Data
    @Schema(name = "BoTermsDetailRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoTermsDetailResponse")
    public static class Response {
        private List<TermsItem> terms;

        @Data
        @Schema(name = "BoTermsDetailTermsItem")
        public static class TermsItem {
            private Long id;
            private String title;
            private String contents;
            private LocalDateTime createAt;
        }
    }
}
