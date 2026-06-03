package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class PopularSearchDto {
    @Data
    @Schema(name = "PopularSearchRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "PopularSearchResponse")
    public static class Response {
        private List<RankItem> rank;

        @Data
        @Schema(name = "PopularSearchRankItem")
        public static class RankItem {
            private Long id;
            private String keyword;
        }
    }
}
