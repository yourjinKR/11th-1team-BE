package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoommateRequestListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<RoommateRequest> roommateRequests;

        @Data
        public static class RoommateRequest {
            private Long requester;
            private Long reqeustee;
            private LocalDateTime createAt;
            private Long chatRoomId;
            private Boolean isAgree;
        }
    }
}
