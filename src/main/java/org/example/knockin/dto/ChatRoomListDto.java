package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatRoomListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<ChatRoom> chatRooms;

        @Data
        public static class ChatRoom {
            private String name;
            private LocalDateTime creatAt;
            private Long chatRoomId;
            private Boolean isAgree;
        }
    }
}
