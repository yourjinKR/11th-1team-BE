package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateMatchingRequiredAlarm;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateMatchingRequiredAlarmServiceImpl {
    private final BasicInformationServiceImpl basicInformationService;
    private final AlarmServiceImpl alarmService;
    @Value("${policy.request-alarm.expire-days}")
    private int requestAlarmExpireDays;

    public void send(Member receiver, Member actor, RoommateMatchingRequired required, String alarmTemplate) {
        BasicInformation basicInformation = basicInformationService.findLatestBasicInformation(actor);
        String actorName = basicInformation.getName();
        String message = String.format(alarmTemplate, actorName);

        RoommateMatchingRequiredAlarm alarm = RoommateMatchingRequiredAlarm.builder()
                .member(receiver)
                .title(message)
                .contents(message)
                .isRead(false)
                .expiredAt(LocalDateTime.now().plusDays(requestAlarmExpireDays))
                .type(AlarmType.OFFER)
                .roommateMatchingRequired(required)
                .build();

        alarmService.sendToClient(receiver.getId(), AlarmType.OFFER.name(), alarm);
    }
}
