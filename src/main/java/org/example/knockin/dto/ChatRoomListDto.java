package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.example.knockin.entity.chat.ChattingRequiredStatus;

@Data
public class ChatRoomListDto {
    @Data
    public static class Request {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "채팅방 식별 고유 ID")
        private Long chatRoomId;

        @Schema(description = "상대방 이름")
        private String memberName;

        @Schema(description = "상대방 프로필 사진 URL")
        private String memberProfileImageUrl;

        @Schema(description = "채팅방 생성 일자")
        private LocalDateTime createdAt;

        @Schema(description = "채팅 요청 상태")
        private ChattingRequiredStatus status;
    }
}
