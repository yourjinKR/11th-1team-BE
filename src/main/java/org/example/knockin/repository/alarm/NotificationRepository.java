package org.example.knockin.repository.alarm;

import org.example.knockin.entity.alarm.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {
    Notification findByIdAndIsDeleted(Long id, Boolean isDeleted);
}