package org.example.knockin.repository.room.Impl;

import static org.example.knockin.entity.member.QBasicInformation.basicInformation;
import static org.example.knockin.entity.room.QExcludeRoommateCalendar.excludeRoommateCalendar;
import static org.example.knockin.entity.room.QRepeatRoommateCalendar.repeatRoommateCalendar;
import static org.example.knockin.entity.room.QRoommateCalendar.roommateCalendar;
import static org.example.knockin.entity.room.QRoommateCalendarCategory.roommateCalendarCategory;
import static org.example.knockin.entity.room.QRoommateCalendarMember.roommateCalendarMember;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.room.QRepeatRoommateCalendar;
import org.example.knockin.repository.room.RoommateCalendarRepositoryCustom;
import org.example.knockin.repository.room.row.DailyCalendarMemberRow;
import org.example.knockin.repository.room.row.DailyCalendarRow;
import org.example.knockin.repository.room.row.MonthlyCalendarRow;
import org.example.knockin.repository.room.row.RepeatCalendarExcludeRow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoommateCalendarRepositoryImpl implements RoommateCalendarRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<DailyCalendarRow> findDailyCalendarList(Long myRoommateId, LocalDateTime from, LocalDateTime to) {
        QRepeatRoommateCalendar latestRepeat = new QRepeatRoommateCalendar("latestRepeat");

        return jpaQueryFactory
                .select(Projections.constructor(
                        DailyCalendarRow.class,
                        roommateCalendar.id,
                        roommateCalendar.title,
                        roommateCalendar.contents,
                        roommateCalendar.startDate,
                        roommateCalendar.endDate,
                        roommateCalendarCategory.name,
                        repeatRoommateCalendar.id,
                        repeatRoommateCalendar.endDate,
                        repeatRoommateCalendar.repeatType
                ))
                .from(roommateCalendar)
                .join(roommateCalendar.roommateCalendarCategory, roommateCalendarCategory)
                .leftJoin(repeatRoommateCalendar)
                .on(repeatRoommateCalendar.id.eq(
                        JPAExpressions
                                .select(latestRepeat.id.max())
                                .from(latestRepeat)
                                .where(latestRepeat.roommateCalendar.eq(roommateCalendar))
                ))
                .where(
                        roommateCalendar.myRoommate.id.eq(myRoommateId),
                        calendarRange(from, to),
                        roommateCalendar.isDeleted.isFalse()
                )
                .orderBy(roommateCalendar.startDate.asc(), roommateCalendar.id.asc())
                .fetch();
    }

    @Override
    public List<MonthlyCalendarRow> findMonthlyCalendarList(Long myRoommateId, LocalDateTime from, LocalDateTime to) {
        QRepeatRoommateCalendar latestRepeat = new QRepeatRoommateCalendar("latestRepeat");

        return jpaQueryFactory
                .select(Projections.constructor(
                        MonthlyCalendarRow.class,
                        roommateCalendar.id,
                        roommateCalendar.startDate,
                        roommateCalendar.endDate,
                        repeatRoommateCalendar.id,
                        repeatRoommateCalendar.endDate,
                        repeatRoommateCalendar.repeatType
                ))
                .from(roommateCalendar)
                .leftJoin(repeatRoommateCalendar)
                .on(repeatRoommateCalendar.id.eq(
                        JPAExpressions
                                .select(latestRepeat.id.max())
                                .from(latestRepeat)
                                .where(latestRepeat.roommateCalendar.eq(roommateCalendar))
                ))
                .where(
                        roommateCalendar.myRoommate.id.eq(myRoommateId),
                        calendarRange(from, to),
                        roommateCalendar.isDeleted.isFalse()
                )
                .orderBy(roommateCalendar.startDate.asc(), roommateCalendar.id.asc())
                .fetch();
    }

    @Override
    public List<DailyCalendarMemberRow> findDailyCalendarMembers(List<Long> calendarIds) {
        if (calendarIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        DailyCalendarMemberRow.class,
                        roommateCalendarMember.roommateCalendar.id,
                        roommateCalendarMember.member.id,
                        basicInformation.name
                ))
                .from(roommateCalendarMember)
                .leftJoin(basicInformation)
                .on(basicInformation.id.eq(
                        JPAExpressions
                                .select(basicInformation.id.max())
                                .from(basicInformation)
                                .where(basicInformation.member.id.eq(roommateCalendarMember.member.id))
                ))
                .where(roommateCalendarMember.roommateCalendar.id.in(calendarIds))
                .orderBy(roommateCalendarMember.roommateCalendar.id.asc(), roommateCalendarMember.member.id.asc())
                .fetch();
    }

    @Override
    public List<RepeatCalendarExcludeRow> findRepeatCalendarExcludes(List<Long> repeatCalendarIds) {
        if (repeatCalendarIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        RepeatCalendarExcludeRow.class,
                        excludeRoommateCalendar.repeatRoommateCalendar.id,
                        excludeRoommateCalendar.excludeAt
                ))
                .from(excludeRoommateCalendar)
                .where(excludeRoommateCalendar.repeatRoommateCalendar.id.in(repeatCalendarIds))
                .fetch();
    }

    private BooleanExpression calendarRange(LocalDateTime from, LocalDateTime to) {
        BooleanExpression basicCalendarRange = repeatRoommateCalendar.id.isNull()
                .and(roommateCalendar.startDate.lt(to))
                .and(roommateCalendar.endDate.gt(from));

        BooleanExpression repeatCalendarRange = repeatRoommateCalendar.id.isNotNull()
                .and(roommateCalendar.startDate.lt(to))
                .and(repeatRoommateCalendar.endDate.gt(from));

        return basicCalendarRange.or(repeatCalendarRange);
    }

}
