package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
        @Schema(description = "방 유형")
        private List<RoomTypeItem> roomType;

        @Data
        @Builder
        public static class RoomTypeItem {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "이름")
            private String name;
        }
    }
}