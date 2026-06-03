package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class InquiryCategoryListDto {
    @Data
    @Schema(name = "InquiryCategoryListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "InquiryCategoryListResponse")
    public static class Response {
        private List<Category> inquirieCategorys;

        @Data
        @Schema(name = "InquiryCategoryListCategory")
        public static class Category {
            private Long id;
            private String name;
        }
    }
}
