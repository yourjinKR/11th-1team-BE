package org.example.knockin.repository.chat;

import org.example.knockin.entity.chat.ChattingRequiredAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingRequiredAlarmRepository extends JpaRepository<ChattingRequiredAlarm, Long> {
}