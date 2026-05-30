package org.example.knockin.repository.alarm;

import org.example.knockin.entity.alarm.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}