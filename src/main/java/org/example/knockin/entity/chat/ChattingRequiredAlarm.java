package org.example.knockin.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.knockin.entity.alarm.Alarm;
import org.example.knockin.entity.alarm.AlarmType;
import org.example.knockin.entity.room.RoomProfileType;


@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatting_required_alarm")
@DiscriminatorValue(AlarmType.Values.CHATTING_REQUIRED)
public class ChattingRequiredAlarm extends Alarm{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_required_id", nullable = false)
    private ChattingRequired chattingRequired;
}
