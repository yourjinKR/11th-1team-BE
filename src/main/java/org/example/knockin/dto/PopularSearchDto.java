package org.example.knockin.dto;

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
        private List<RankItem> rank;

        @Data
        @Builder
        public static class RankItem {
            private Long id;
            private String keyword;
        }
    }
}
