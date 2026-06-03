package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class MetaRoomTypesDto {
    @Data
    @Schema(name = "MetaRoomTypesRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "MetaRoomTypesResponse")
    public static class Response {
        private List<RoomTypeItem> roomType;

        @Data
        @Schema(name = "MetaRoomTypesRoomTypeItem")
        public static class RoomTypeItem {
            private Long id;
            private String name;
        }
    }
}
