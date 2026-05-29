package org.example.knockin.repository.alarm;

import org.example.knockin.entity.alarm.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}