package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.AlarmErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AlarmServiceImpl {
    private final MemberServiceImpl memberService;
    private final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long memberId) {
        long timeout = 1000L * 60 * 60;
        SseEmitter sseEmitter = new SseEmitter(timeout);
        sseEmitterMap.put(memberId, sseEmitter);
        sseEmitter.onCompletion(() -> sseEmitterMap.remove(memberId));
        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError(throwable -> sseEmitter.complete());
        return sseEmitter;
    }

    public void sendToClient(Long memberId, String eventName, Object data) {
        SseEmitter sseEmitter = sseEmitterMap.get(memberId);
        try {
            String eventId = memberId + "_" + System.currentTimeMillis();
            sseEmitter.send(SseEmitter.event().id(eventId).name(eventName).data(data));
        } catch (IOException e) {
            sseEmitterMap.remove(memberId);
            throw new BusinessException(AlarmErrorCode.ALARM_SEND_ERROR);
        }
    }
}
