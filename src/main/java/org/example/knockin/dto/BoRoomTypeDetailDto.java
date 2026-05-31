package org.example.knockin.dto;

import lombok.Data;

@Data
public class BoRoomTypeDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
    }
}
