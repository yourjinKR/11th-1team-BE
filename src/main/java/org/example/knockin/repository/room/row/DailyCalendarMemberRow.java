package org.example.knockin.repository.room.row;

public record DailyCalendarMemberRow(
        Long calendarId,
        Long memberId,
        String name
) {
}
