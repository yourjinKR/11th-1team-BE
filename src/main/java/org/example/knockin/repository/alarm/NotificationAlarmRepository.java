package org.example.knockin.repository.alarm;

import org.example.knockin.entity.alarm.NotificationAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationAlarmRepository extends JpaRepository<NotificationAlarm, Long> {
}