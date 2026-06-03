package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfileVisibilityDto {
    @Data
    @Schema(name = "ProfileVisibilityRequest")
    public static class Request {
        private String status;
    }

    @Data
    @Schema(name = "ProfileVisibilityResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
