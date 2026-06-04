package org.example.knockin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeleteUserDto {
    @Builder
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
