package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoRoomTypeDto {
    @Data
    @Schema(name = "BoRoomTypeRequest")
    public static class Request {
        private String name;
    }

    @Data
    @Schema(name = "BoRoomTypeResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
