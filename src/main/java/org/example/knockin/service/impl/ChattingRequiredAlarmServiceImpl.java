package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.chat.ChattingRequired;
import org.example.knockin.entity.chat.ChattingRequiredAlarm;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChattingRequiredAlarmServiceImpl {

    private static final Integer ALARM_EXPIRE_DAYS = 7;

    private final BasicInformationServiceImpl basicInformationService;
    private final AlarmServiceImpl alarmService;

    public void send(Member receiver, Member actor, ChattingRequired chattingRequired, String alarmTemplate) {
        BasicInformation basicInformation = basicInformationService.findLatestBasicInformation(actor);
        String actorName = basicInformation.getName();
        String message = String.format(alarmTemplate, actorName);

        ChattingRequiredAlarm alarm = ChattingRequiredAlarm.builder()
                .member(receiver)
                .title(message)
                .contents(message)
                .isRead(false)
                .expiredAt(LocalDateTime.now().plusDays(ALARM_EXPIRE_DAYS))
                .type(AlarmType.CHATTING_REQUIRED)
                .chattingRequired(chattingRequired)
                .build();

        alarmService.sendToClient(receiver.getId(), AlarmType.CHATTING_REQUIRED.name(), alarm);
    }
}
