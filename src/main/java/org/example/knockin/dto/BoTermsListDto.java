package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
        @Schema(description = "약관")
        private List<TermsItem> terms;

        @Data
        public static class TermsItem {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "제목")
            private String title;
            @Schema(description = "생성 일시")
            private LocalDateTime createAt;
        }
    }
}