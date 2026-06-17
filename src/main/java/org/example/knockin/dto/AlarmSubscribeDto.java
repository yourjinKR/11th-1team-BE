package org.example.knockin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

@Data
public class AlarmSubscribeDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
    }

    @Data
    @Builder
    public static class Response {
        private SseEmitter sseEmitter;
    }
}
