package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.CalendarDto;
import org.example.knockin.dto.CalendarDto.CalendarInfoDto;
import org.example.knockin.dto.CalendarDto.CalendarMemberDto;
import org.example.knockin.dto.RepeatCalendarDto;
import org.example.knockin.dto.RepeatCalendarDto.RepeatCalendarInfo;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RepeatRoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarCategory;
import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MyRoommateErrorCode;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.example.knockin.repository.room.RepeatRoommateCalendarRepository;
import org.example.knockin.repository.room.RoommateCalendarCategoryRepository;
import org.example.knockin.repository.room.RoommateCalendarMemberRepository;
import org.example.knockin.repository.room.RoommateCalendarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl {

    private final RoommateCalendarCategoryRepository roommateCalendarCategoryRepository;
    private final MyRoommateRepository myRoommateRepository;
    private final RoommateCalendarRepository roommateCalendarRepository;
    private final RoommateCalendarMemberRepository roommateCalendarMemberRepository;
    private final RepeatRoommateCalendarRepository repeatRoommateCalendarRepository;

    @Transactional
    public CalendarDto.Response saveBasicCalendar(Long memberId, CalendarDto.Request request) {
        MyRoommate myRoommate = myRoommateRepository.findWithFetchedByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        RoommateCalendarCategory roommateCalendarCategory = saveCalendarCategory(request.getCategoryName());
        RoommateCalendar roommateCalendar = saveCalendar(memberId, myRoommate, roommateCalendarCategory, request.getCalendar());
        saveCalendarMembers(roommateCalendar, roommateMatchingRequired, request.getMembers());
        return CalendarDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    private RoommateCalendar saveCalendar(Long memberId, MyRoommate myRoommate, RoommateCalendarCategory category, CalendarInfoDto dto) {
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        Member me = pickMe(memberId, roommateMatchingRequired);

        return roommateCalendarRepository.save(RoommateCalendar.builder()
                .myRoommate(myRoommate)
                .member(me)
                .roommateCalendarCategory(category)
                .title(dto.getTitle())
                .contents(dto.getContents())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build());
    }

    private RoommateCalendarCategory saveCalendarCategory(String name) {
        RoommateCalendarCategory category = RoommateCalendarCategory.builder()
                .name(name)
                .build();
        return roommateCalendarCategoryRepository.save(category);
    }

    private List<RoommateCalendarMember> saveCalendarMembers(RoommateCalendar calendar, RoommateMatchingRequired required, List<CalendarMemberDto> dtos) {
        List<RoommateCalendarMember> roommateCalendarMembers = dtos.stream()
                .map(dto -> {
                    Member member = pickMe(dto.getMemberId(), required);
                    return RoommateCalendarMember.of(calendar, member);
                })
                .toList();
        return roommateCalendarMemberRepository.saveAll(roommateCalendarMembers);
    }

    private Member pickMe(Long memberId, RoommateMatchingRequired roommateMatchingRequired) {
        Member requester = roommateMatchingRequired.getRequester();
        Member requestee = roommateMatchingRequired.getRequestee();
        return Objects.equals(requester.getId(), memberId) ? requester : requestee;
    }

    @Transactional
    public RepeatCalendarDto.Response saveRepeatCalendar(Long memberId, RepeatCalendarDto.Request request) {
        MyRoommate myRoommate = myRoommateRepository.findWithFetchedByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        RoommateCalendarCategory roommateCalendarCategory = saveCalendarCategory(request.getCategoryName());
        RoommateCalendar roommateCalendar = saveCalendar(memberId, myRoommate, roommateCalendarCategory, request.getCalendar());
        saveCalendarMembers(roommateCalendar, roommateMatchingRequired, request.getMembers());
        saveRepeatRoommateCalendar(roommateCalendar, request.getRepeatInfo());
        return RepeatCalendarDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    private RepeatRoommateCalendar saveRepeatRoommateCalendar(RoommateCalendar calendar, RepeatCalendarInfo repeatInfo) {
        RepeatRoommateCalendar repeatRoommateCalendar = RepeatRoommateCalendar.builder()
                .roommateCalendar(calendar)
                .endDate(repeatInfo.getEndDate())
                .repeatType(repeatInfo.getRepeatType())
                .build();
        return repeatRoommateCalendarRepository.save(repeatRoommateCalendar);
    }
}
