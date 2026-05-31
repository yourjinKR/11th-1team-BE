package org.example.knockin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatRequestListDto {
    @Data
    public static class Request {
    }

    @Data
    public static class Response {
        private List<ChatRequired> chatRequireds;

        @Data
        public static class ChatRequired {
            private String name;
            private String type;
            private Integer score;
            private LocalDateTime creatAt;
            private Long chatReqId;
        }
    }
}
