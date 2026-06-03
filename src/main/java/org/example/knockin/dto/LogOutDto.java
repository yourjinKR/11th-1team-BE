package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogOutDto {
    @Data
    @Schema(name = "LogOutRequest")
    public static class Request {
        private String accessToken;
    }

    @Data
    @Schema(name = "LogOutResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
