package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class MetaRegionsDto {
    @Data
    @Schema(name = "MetaRegionsRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MetaRegionsResponse")
    public static class Response {
        private List<RegionItem> region;

        @Data
        @Schema(name = "MetaRegionsRegionItem")
        public static class RegionItem {
            private Long id;
            private String name;
            private Long parentId;
        }
    }
}
