package org.example.knockin.entity.room;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.knockin.entity.alarm.Alarm;
import org.example.knockin.entity.alarm.AlarmType;


@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roommate_matching_required_alarm")
@DiscriminatorValue(AlarmType.Values.ROOM_MATCHING)
public class RoommateMatchingRequiredAlarm extends Alarm{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roommate_matching_required_id", nullable = false)
    private RoommateMatchingRequired roommateMatchingRequired;
}
