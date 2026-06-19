package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
public class BoRoomTypeDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "고유 식별 ID")
        private Long id;
        @Schema(description = "이름")
        private String name;
    }
}