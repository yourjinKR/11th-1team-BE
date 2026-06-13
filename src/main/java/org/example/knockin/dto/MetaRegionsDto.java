package org.example.knockin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class MetaRegionsDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private List<RegionItem> region;

        @Data
        @Builder
        public static class RegionItem {
            private Long id;
            private String name;
            private Long parentId;
        }
    }
}
