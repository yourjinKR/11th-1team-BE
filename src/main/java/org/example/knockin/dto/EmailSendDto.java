package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class EmailSendDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
