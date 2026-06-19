package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class MetaRoomAddOptionsDto {
    @Data
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        @Schema(description = "방 추가 옵션")
        private List<RoomAddOptionItem> roomAddOption;

        @Data
        @Builder
        public static class RoomAddOptionItem {
            @Schema(description = "고유 식별 ID")
            private Long id;
            @Schema(description = "이름")
            private String name;
        }
    }
}