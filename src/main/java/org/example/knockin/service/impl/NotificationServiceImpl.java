package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoNoticeDetailDto;
import org.example.knockin.dto.BoNoticeListDto;
import org.example.knockin.entity.alarm.Notification;
import org.example.knockin.repository.alarm.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {
    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<BoNoticeListDto.Response.NoticeItem> findNotificationList(Pageable pageable) {
        return notificationRepository.findNotificationsByIsDeleted(false, pageable);
    }

    public Notification findNotificationById(Long id) {
        return notificationRepository.findByIdAndIsDeleted(id, false);
    }

    public BoNoticeDetailDto.Response findNotification(Long id) {
        return notificationRepository.findNotificationByIsDeleted(false, id);
    }
}
