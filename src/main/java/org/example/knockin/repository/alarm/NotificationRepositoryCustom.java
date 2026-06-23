package org.example.knockin.repository.alarm;

import org.example.knockin.dto.BoNoticeDetailDto;
import org.example.knockin.dto.BoNoticeListDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepositoryCustom  {
    List<BoNoticeListDto.Response.NoticeItem> findNotificationsByIsDeleted(Boolean isDeleted, Pageable pageable);
    BoNoticeDetailDto.Response findNotificationByIsDeleted(Boolean isDeleted, Long id);
}