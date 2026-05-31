package org.example.knockin.dto;

import lombok.Data;

@Data
public class MyRoommateCalendarDetailDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String contents;
    }
}
