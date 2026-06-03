package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoTermsDto {
    @Data
    @Schema(name = "BoTermsRequest")
    public static class Request {
        private String title;
        private String contents;
    }

    @Data
    @Schema(name = "BoTermsResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
