package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class CalendarTypesDto {
    @Data
    @Schema(name = "CalendarTypesRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "CalendarTypesResponse")
    public static class Response {
        private List<Type> types;

        @Data
        @Schema(name = "CalendarTypesType")
        public static class Type {
            private Long id;
            private String name;
        }
    }
}
