package org.example.knockin.repository.room.row;

import java.time.LocalDateTime;
import org.example.knockin.entity.room.RepeatType;

public record DailyCalendarRow(
        Long calendarId,
        String title,
        String contents,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String categoryName,
        Long repeatCalendarId,
        LocalDateTime repeatEndDate,
        RepeatType repeatType
) {
    public boolean isRepeat() {
        return repeatCalendarId != null;
    }
}
