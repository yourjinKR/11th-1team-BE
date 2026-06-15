package org.example.knockin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class MetaRoomTypesDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private List<RoomTypeItem> roomType;

        @Data
        @Builder
        public static class RoomTypeItem {
            private Long id;
            private String name;
        }
    }
}
