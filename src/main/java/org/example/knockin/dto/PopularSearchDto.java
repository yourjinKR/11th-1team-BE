package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class PopularSearchDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<RankItem> rank;

        @Data
        public static class RankItem {
            private Long id;
            private String keyword;
        }
    }
}
