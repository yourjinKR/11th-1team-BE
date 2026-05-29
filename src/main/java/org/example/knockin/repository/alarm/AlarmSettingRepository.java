package org.example.knockin.repository.alarm;

import org.example.knockin.entity.alarm.AlarmSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmSettingRepository extends JpaRepository<AlarmSetting, Long> {
}