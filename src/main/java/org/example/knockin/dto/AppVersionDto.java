package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AppVersionDto {
    @Data
    public static class Request {

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        @Schema(description = "고유 번호")
        private Long id;
        @Schema(description = "앱 버전")
        private String version;
    }
}
