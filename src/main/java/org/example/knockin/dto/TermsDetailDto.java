package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TermsDetailDto {
    @Data
    @Schema(name = "TermsDetailRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "TermsDetailResponse")
    public static class Response {
        private Long id;
        private String contents;
    }
}
