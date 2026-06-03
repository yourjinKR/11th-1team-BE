package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoommateRequestDto {
    @Data
    @Schema(name = "RoommateRequestRequest")
    public static class Request {
        private Long chatRoomId;
    }

    @Data
    @Schema(name = "RoommateRequestResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
