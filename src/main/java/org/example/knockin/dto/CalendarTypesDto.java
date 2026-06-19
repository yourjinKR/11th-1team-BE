package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.util.List;

@Data
public class CalendarTypesDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "types")
        private List<Type> types;

        @Data
        public static class Type {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "이름")
            private String name;
        }
    }
}