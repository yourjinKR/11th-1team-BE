package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BoRoomTypeDetailDto {
    @Data
    @Schema(name = "BoRoomTypeDetailRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoRoomTypeDetailResponse")
    public static class Response {
        private Long id;
        private String name;
    }
}
