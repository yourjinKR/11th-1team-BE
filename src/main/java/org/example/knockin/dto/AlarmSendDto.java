package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

@Data
public class AlarmSendDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String contents;
        private boolean isRead;
        private LocalDateTime expiredAt;
        private LocalDateTime createdAt;
    }
}
