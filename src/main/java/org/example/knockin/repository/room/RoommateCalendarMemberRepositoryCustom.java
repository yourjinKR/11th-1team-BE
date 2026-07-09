package org.example.knockin.repository.room;

import java.util.List;
import org.example.knockin.repository.room.row.DailyCalendarMemberRow;

public interface RoommateCalendarMemberRepositoryCustom {
    List<DailyCalendarMemberRow> findDailyCalendarMembers(List<Long> calendarIds);
}
