package org.example.knockin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoardDto {
    @Data
    public static class Request {
        private String title;
        private String contents;
        private Integer deposit;
        private Integer mountlyRent;
        private Integer managementCost;
        private Long roomType;
        private Long region;
        private LocalDateTime comeableAt;
        private List<ImageDto> images;

        @Data
        public static class ImageDto {
            private String image;
            private Boolean thumnail;
        }
    }

    @Data
    public static class Response {
        private LocalDateTime updatedAt;
    }
}
