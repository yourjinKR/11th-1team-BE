package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.knockin.entity.room.RoommateRequiredStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoommateRequestListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        @Schema(description = "룸메이트 요청 목록")
        private List<RoommateRequest> roommateRequests;

        @Data
        public static class RoommateRequest {
            @Schema(description = "요청자 ID")
            private Long requester;
            @Schema(description = "수신자 ID")
            private Long reqeustee;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
            @Schema(description = "채팅방 ID")
            private Long chatRoomId;
            @Schema(description = "룸메이트 요청 상태")
            private RoommateRequiredStatus status;
        }
    }
}
