package org.example.knockin.repository.room;

import java.time.LocalDateTime;
import java.util.List;
import org.example.knockin.repository.room.row.DailyCalendarMemberRow;
import org.example.knockin.repository.room.row.DailyCalendarRow;
import org.example.knockin.repository.room.row.RepeatCalendarExcludeRow;

public interface RoommateCalendarRepositoryCustom {
    List<DailyCalendarRow> findDailyCalendarList(Long myRoommateId, LocalDateTime from, LocalDateTime to);

    List<DailyCalendarMemberRow> findDailyCalendarMembers(List<Long> calendarIds);

    List<RepeatCalendarExcludeRow> findRepeatCalendarExcludes(List<Long> repeatCalendarIds);
}
