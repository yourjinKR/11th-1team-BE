package org.example.knockin.repository.room.row;

import java.time.LocalDateTime;

public record RepeatCalendarExcludeRow(
        Long repeatCalendarId,
        LocalDateTime excludeAt
) {
}
