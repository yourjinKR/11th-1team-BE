package org.example.knockin.entity.room;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.knockin.entity.alarm.Alarm;
import org.example.knockin.entity.alarm.AlarmType.Values;

@Getter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roommate_calendar_alarm")
@DiscriminatorValue(Values.ROOMMATE_CALENDAR)
public class RoommateCalendarAlarm extends Alarm {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roommate_calendar_id", nullable = false)
    private RoommateCalendar roommateCalendar;
}
