package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class MetaRoomAddOptionsDto {
    @Data
    @Schema(name = "MetaRoomAddOptionsRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MetaRoomAddOptionsResponse")
    public static class Response {
        private List<RoomAddOptionItem> roomAddOption;

        @Data
        @Schema(name = "MetaRoomAddOptionsRoomAddOptionItem")
        public static class RoomAddOptionItem {
            private Long id;
            private String name;
        }
    }
}
