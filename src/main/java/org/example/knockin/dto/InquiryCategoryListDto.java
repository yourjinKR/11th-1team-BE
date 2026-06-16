package org.example.knockin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class InquiryCategoryListDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private List<Category> inquirieCategorys;

        @Data
        @Builder
        public static class Category {
            private Long id;
            private String name;
        }
    }
}
