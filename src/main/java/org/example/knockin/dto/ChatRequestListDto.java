package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatRequestListDto {
    @Data
    @Schema(name = "ChatRequestListRequest")
    public static class Request {
    }

    @Data
    @Schema(name = "ChatRequestListResponse")
    public static class Response {
        private List<ChatRequired> chatRequireds;

        @Data
        @Schema(name = "ChatRequestListChatRequired")
        public static class ChatRequired {
            private String name;
            private String type;
            private Integer score;
            private LocalDateTime creatAt;
            private Long chatReqId;
        }
    }
}
