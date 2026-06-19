package org.example.knockin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatRoomDto {

    @Data
    public static class Request { }

    @Data
    @AllArgsConstructor
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
