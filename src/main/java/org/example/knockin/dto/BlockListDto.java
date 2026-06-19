package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlockListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "blocks")
        private List<Block> blocks;

        @Data
        public static class Block {
            @Schema(description = "사용자 id")
            private Long userId;
            @Schema(description = "이름")
            private String name;
            @Schema(description = "생성 일시")
            private LocalDateTime createAt;
        }
    }
}