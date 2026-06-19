package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
        @Schema(description = "채팅 requireds")
        private List<ChatRequired> chatRequireds;

        @Data
        public static class ChatRequired {
            @Schema(description = "이름")
            private String name;
            @Schema(description = "유형")
            private String type;
            @Schema(description = "점수")
            private Integer score;
            @Schema(description = "creat at")
            private LocalDateTime creatAt;
            @Schema(description = "채팅 req id")
            private Long chatReqId;
        }
    }
}