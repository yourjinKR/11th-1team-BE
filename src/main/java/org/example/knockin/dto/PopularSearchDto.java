package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class PopularSearchDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        @Schema(description = "rank")
        private List<RankItem> rank;

        @Data
        @Builder
        public static class RankItem {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "keyword")
            private String keyword;
        }
    }
}