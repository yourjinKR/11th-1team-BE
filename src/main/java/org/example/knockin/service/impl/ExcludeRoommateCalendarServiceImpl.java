package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.room.ExcludeRoommateCalendar;
import org.example.knockin.entity.room.RepeatRoommateCalendar;
import org.example.knockin.repository.room.ExcludeRoommateCalendarRepository;
import org.example.knockin.repository.room.row.RepeatCalendarExcludeRow;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcludeRoommateCalendarServiceImpl {

    private final ExcludeRoommateCalendarRepository excludeRoommateCalendarRepository;

        public ExcludeRoommateCalendar save(RepeatRoommateCalendar repeatCalendar, LocalDateTime excludeAt) {
            ExcludeRoommateCalendar excludeCalendar = ExcludeRoommateCalendar.builder()
                    .repeatRoommateCalendar(repeatCalendar)
                    .excludeAt(excludeAt)
                    .build();
            return excludeRoommateCalendarRepository.save(excludeCalendar);
    }

    public List<RepeatCalendarExcludeRow> findRepeatCalendarExcludes(List<Long> repeatCalendarIds) {
        return excludeRoommateCalendarRepository.findRepeatCalendarExcludes(repeatCalendarIds);
    }
}
