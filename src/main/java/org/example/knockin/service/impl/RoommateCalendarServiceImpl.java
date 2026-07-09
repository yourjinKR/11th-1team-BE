package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.CalendarDto.CalendarInfoDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarCategory;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.MyRoommateErrorCode;
import org.example.knockin.repository.room.RoommateCalendarRepository;
import org.example.knockin.repository.room.row.DailyCalendarRow;
import org.example.knockin.repository.room.row.MonthlyCalendarRow;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateCalendarServiceImpl {

    private final RoommateCalendarRepository roommateCalendarRepository;

    public RoommateCalendar save(MyRoommate myRoommate, Member member, RoommateCalendarCategory category, CalendarInfoDto dto) {
        RoommateCalendar roommateCalendar = RoommateCalendar.builder()
                .myRoommate(myRoommate)
                .member(member)
                .roommateCalendarCategory(category)
                .title(dto.getTitle())
                .contents(dto.getContents())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        return roommateCalendarRepository.save(roommateCalendar);
    }

    public RoommateCalendar findByIdOrThrow(Long calendarId) {
        return roommateCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new BusinessException(MyRoommateErrorCode.CALENDER_NOT_FOUND));
    }

    public List<DailyCalendarRow> findDailyCalendarList(Long myRoommateId, LocalDateTime from, LocalDateTime to) {
        return roommateCalendarRepository.findDailyCalendarList(myRoommateId, from, to);
    }

    public List<MonthlyCalendarRow> findMonthlyCalendarList(Long myRoommateId, LocalDateTime from, LocalDateTime to) {
        return roommateCalendarRepository.findMonthlyCalendarList(myRoommateId, from, to);
    }
}
