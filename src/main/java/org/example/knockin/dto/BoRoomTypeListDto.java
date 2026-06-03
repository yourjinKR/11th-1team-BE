package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class BoRoomTypeListDto {
    @Data
    @Schema(name = "BoRoomTypeListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "BoRoomTypeListResponse")
    public static class Response {
        private List<RoomTypeItem> roomType;

        @Data
        @Schema(name = "BoRoomTypeListRoomTypeItem")
        public static class RoomTypeItem {
            private Long id;
            private String name;
        }
    }
}
