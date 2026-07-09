package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.RepeatCalendarDto.RepeatCalendarInfo;
import org.example.knockin.entity.room.RepeatRoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.MyRoommateErrorCode;
import org.example.knockin.repository.room.RepeatRoommateCalendarRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepeatRoommateCalendarServiceImpl {

    private final RepeatRoommateCalendarRepository repeatRoommateCalendarRepository;

    public RepeatRoommateCalendar save(RoommateCalendar calendar, RepeatCalendarInfo repeatInfo) {
        RepeatRoommateCalendar repeatRoommateCalendar = RepeatRoommateCalendar.builder()
                .roommateCalendar(calendar)
                .endDate(repeatInfo.getEndDate())
                .repeatType(repeatInfo.getRepeatType())
                .build();
        return repeatRoommateCalendarRepository.save(repeatRoommateCalendar);
    }

    public RepeatRoommateCalendar findOneByRoommateCalendarOrThrow(RoommateCalendar calendar) {
        return repeatRoommateCalendarRepository.findOneByRoommateCalendar(calendar)
                .orElseThrow(() -> new BusinessException(MyRoommateErrorCode.CALENDER_NOT_REPEAT));
    }
}
