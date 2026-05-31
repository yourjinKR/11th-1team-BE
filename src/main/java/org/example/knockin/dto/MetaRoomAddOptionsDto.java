package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class MetaRoomAddOptionsDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<RoomAddOptionItem> roomAddOption;

        @Data
        public static class RoomAddOptionItem {
            private Long id;
            private String name;
        }
    }
}
