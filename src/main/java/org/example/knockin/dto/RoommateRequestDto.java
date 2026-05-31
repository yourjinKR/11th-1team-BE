package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoommateRequestDto {
    @Data
    public static class Request {
        private Long chatRoomId;
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
