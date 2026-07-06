package org.example.knockin.repository.room.row;

import java.time.LocalDateTime;
import org.example.knockin.entity.room.RepeatType;

public record MonthlyCalendarRow(
        Long calendarId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long repeatCalendarId,
        LocalDateTime repeatEndDate,
        RepeatType repeatType
) {
    public boolean isRepeat() {
        return repeatCalendarId != null;
    }
}
