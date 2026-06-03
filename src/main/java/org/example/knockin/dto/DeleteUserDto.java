package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeleteUserDto {
    @Data
    @Schema(name = "DeleteUserResponse")
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
