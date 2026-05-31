package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class InquiryCategoryListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<Category> inquirieCategorys;

        @Data
        public static class Category {
            private Long id;
            private String name;
        }
    }
}
