package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockDto {
    @Data
    @Schema(name = "BlockRequest")
    public static class Request {
        private Long userId;
    }

    @Data
    @Schema(name = "BlockResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
