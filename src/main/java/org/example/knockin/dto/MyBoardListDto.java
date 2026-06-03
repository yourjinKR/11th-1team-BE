package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class MyBoardListDto {
    @Data
    @Schema(name = "MyBoardListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MyBoardListResponse")
    public static class Response {
        private List<BoardItem> boards;

        @Data
        @Schema(name = "MyBoardListBoardItem")
        public static class BoardItem {
            private Long boardId;
            private String image;
            private String title;
        }
    }
}
