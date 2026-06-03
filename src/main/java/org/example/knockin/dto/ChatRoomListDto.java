package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatRoomListDto {
    @Data
    @Schema(name = "ChatRoomListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "ChatRoomListResponse")
    public static class Response {
        private List<ChatRoom> chatRooms;

        @Data
        @Schema(name = "ChatRoomListChatRoom")
        public static class ChatRoom {
            private String name;
            private LocalDateTime creatAt;
            private Long chatRoomId;
            private Boolean isAgree;
        }
    }
}
