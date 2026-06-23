package org.example.knockin.repository.alarm.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoNoticeDetailDto;
import org.example.knockin.dto.BoNoticeListDto;
import org.example.knockin.repository.alarm.NotificationRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.knockin.entity.alarm.QNotification.notification;
import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.member.QBasicInformation.basicInformation;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<BoNoticeListDto.Response.NoticeItem> findNotificationsByIsDeleted(Boolean isDeleted, Pageable pageable) {
        return jpaQueryFactory
                .select(Projections.fields(
                        BoNoticeListDto.Response.NoticeItem.class,
                        notification.id,
                        notification.title,
                        notification.createdAt.as("createAt"),
                        basicInformation.name.as("writer")
                ))
                .from(notification)
                .join(notification.member, member)
                .leftJoin(basicInformation).on(basicInformation.member.eq(member))
                .where(notification.isDeleted.eq(isDeleted))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public BoNoticeDetailDto.Response findNotificationByIsDeleted(Boolean isDeleted, Long id) {
        return jpaQueryFactory
                .select(Projections.fields(
                        BoNoticeDetailDto.Response.class,
                        notification.id,
                        notification.title,
                        notification.contents,
                        notification.createdAt.as("createAt"),
                        basicInformation.name.as("writer")
                ))
                .from(notification)
                .join(notification.member, member)
                .leftJoin(basicInformation).on(basicInformation.member.eq(member))
                .where(notification.isDeleted.eq(isDeleted), notification.id.eq(id))
                .fetchOne();
    }
}