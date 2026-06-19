package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoommateRequestDto {
    @Data
    public static class Request {
        @Schema(description = "채팅 방 id")
        private Long chatRoomId;
    }

    @Data
    public static class Response {
        @Schema(description = "수정 일시")
        private LocalDateTime updatedAt;
    }
}