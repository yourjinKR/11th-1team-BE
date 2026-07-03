package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.CalendarDto;
import org.example.knockin.dto.CalendarDto.CalendarMemberDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarCategory;
import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateCalendarMemberId;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MyRoommateErrorCode;
import org.example.knockin.repository.room.MyRoommateRepository;
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

    @Transactional
    public CalendarDto.Response saveCalendar(Long memberId, CalendarDto.Request request) {
        MyRoommate myRoommate = myRoommateRepository.findWithFetchedByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        Member me = pickMe(memberId, roommateMatchingRequired);
        String categoryName = request.getCategoryName();

        RoommateCalendarCategory roommateCalendarCategory = roommateCalendarCategoryRepository.save(
                RoommateCalendarCategory.builder().name(categoryName).build()
        );

        RoommateCalendar roommateCalendar = roommateCalendarRepository.save(RoommateCalendar.builder()
                .myRoommate(myRoommate)
                .member(me)
                .roommateCalendarCategory(roommateCalendarCategory)
                .title(request.getTitle())
                .contents(request.getContents())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build());

        List<CalendarMemberDto> roommateCalendarMemberDtos = request.getMembers();
        List<RoommateCalendarMember> roommateCalendarMembers = roommateCalendarMemberDtos.stream()
                .map(dto -> {
                    Member member = pickMe(dto.getMemberId(), roommateMatchingRequired);
                    return RoommateCalendarMember.of(roommateCalendar, member);
                })
                .toList();
        roommateCalendarMemberRepository.saveAll(roommateCalendarMembers);

        return CalendarDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    private Member pickMe(Long memberId, RoommateMatchingRequired roommateMatchingRequired) {
        Member requester = roommateMatchingRequired.getRequester();
        Member requestee = roommateMatchingRequired.getRequestee();
        return Objects.equals(requester.getId(), memberId) ? requester : requestee;
    }
}
