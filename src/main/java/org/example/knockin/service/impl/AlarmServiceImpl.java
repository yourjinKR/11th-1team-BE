package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.AlarmSendDto;
import org.example.knockin.entity.alarm.Alarm;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.exception.AlarmErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.alarm.AlarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final AlarmRepository alarmRepository;

    public SseEmitter subscribe(Long memberId) {
        memberService.findById(memberId).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        long timeout = 1000L * 60 * 60;
        SseEmitter sseEmitter = new SseEmitter(timeout);
        sseEmitterMap.put(memberId, sseEmitter);
        sseEmitter.onCompletion(() -> sseEmitterMap.remove(memberId));
        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError(throwable -> sseEmitter.complete());
        return sseEmitter;
    }

    @Transactional
    public void sendToClient(Long memberId, String eventName, Alarm alarm) {
        alarmRepository.save(alarm);
        SseEmitter sseEmitter = sseEmitterMap.get(memberId);
        try {
            String eventId = memberId + "_" + System.currentTimeMillis();
            AlarmSendDto.Response response = AlarmSendDto.Response.builder()
                    .id(alarm.getId()).title(alarm.getTitle())
                    .isRead(alarm.getIsRead()).contents(alarm.getContents())
                    .createdAt(alarm.getCreatedAt()).expiredAt(alarm.getExpiredAt()).build();

            sseEmitter.send(SseEmitter.event().id(eventId).name(eventName).data(response));
        } catch (IOException e) {
            sseEmitterMap.remove(memberId);
            throw new BusinessException(AlarmErrorCode.ALARM_SEND_ERROR);
        }
    }
}
