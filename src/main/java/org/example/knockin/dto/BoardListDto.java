package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class BoardListDto {
    @Data
    @Schema(name = "BoardListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoardListResponse")
    public static class Response {
        private List<BoardItem> boards;

        @Data
        @Schema(name = "BoardListBoardItem")
        public static class BoardItem {
            private Long boardId;
            private String image;
        }
    }
}
