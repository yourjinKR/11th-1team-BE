package org.example.knockin.repository.room;

import java.util.List;
import org.example.knockin.repository.room.row.RepeatCalendarExcludeRow;

public interface ExcludeRoommateCalendarRepositoryCustom {
    List<RepeatCalendarExcludeRow> findRepeatCalendarExcludes(List<Long> repeatCalendarIds);
}
