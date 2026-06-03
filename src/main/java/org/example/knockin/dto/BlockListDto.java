package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlockListDto {
    @Data
    @Schema(name = "BlockListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BlockListResponse")
    public static class Response {
        private List<Block> blocks;

        @Data
        @Schema(name = "BlockListBlock")
        public static class Block {
            private Long userId;
            private String name;
            private LocalDateTime createAt;
        }
    }
}
