package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateMatchingRequiredAlarm;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateMatchingRequiredAlarmServiceImpl {

    private static final Integer ALARM_EXPIRE_DAYS = 7;

    private final BasicInformationServiceImpl basicInformationService;
    private final AlarmServiceImpl alarmService;

    public void send(Member receiver, Member actor, RoommateMatchingRequired required, String alarmTemplate) {
        BasicInformation basicInformation = basicInformationService.findLatestBasicInformation(actor);
        String actorName = basicInformation.getName();
        String message = String.format(alarmTemplate, actorName);

        RoommateMatchingRequiredAlarm alarm = RoommateMatchingRequiredAlarm.builder()
                .member(receiver)
                .title(message)
                .contents(message)
                .isRead(false)
                .expiredAt(LocalDateTime.now().plusDays(ALARM_EXPIRE_DAYS))
                .type(AlarmType.OFFER)
                .roommateMatchingRequired(required)
                .build();

        alarmService.sendToClient(receiver.getId(), AlarmType.OFFER.name(), alarm);
    }
}
