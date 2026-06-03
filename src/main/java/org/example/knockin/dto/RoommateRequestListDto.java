package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoommateRequestListDto {
    @Data
    @Schema(name = "RoommateRequestListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "RoommateRequestListResponse")
    public static class Response {
        @Schema(description = "룸메이트 요청 목록")
        private List<RoommateRequest> roommateRequests;

        @Data
        @Schema(name = "RoommateRequestListRoommateRequest")
        public static class RoommateRequest {
            @Schema(description = "요청자 ID")
            private Long requester;
            @Schema(description = "수신자 ID")
            private Long reqeustee;
            @Schema(description = "날짜 및 시간")
            private LocalDateTime createAt;
            @Schema(description = "채팅방 ID")
            private Long chatRoomId;
            @Schema(description = "수락 여부")
            private Boolean isAgree;
        }
    }
}
