package org.example.knockin.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Data
public class AlarmSubscribeDto {
    @Data
    @NoArgsConstructor
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private SseEmitter sseEmitter;
    }
}
