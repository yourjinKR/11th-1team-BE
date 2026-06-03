package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MyRoommateCalendarDetailDto {
    @Data
    @Schema(name = "MyRoommateCalendarDetailRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MyRoommateCalendarDetailResponse")
    public static class Response {
        private Long id;
        private String title;
        private String contents;
    }
}
