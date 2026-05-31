package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class MyBoardListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<BoardItem> boards;

        @Data
        public static class BoardItem {
            private Long boardId;
            private String image;
            private String title;
        }
    }
}
