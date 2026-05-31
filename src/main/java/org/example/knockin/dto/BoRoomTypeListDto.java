package org.example.knockin.dto;

import lombok.Data;
import java.util.List;

@Data
public class BoRoomTypeListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<RoomTypeItem> roomType;

        @Data
        public static class RoomTypeItem {
            private Long id;
            private String name;
        }
    }
}
