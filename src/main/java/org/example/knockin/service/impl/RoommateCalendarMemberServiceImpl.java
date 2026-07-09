package org.example.knockin.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateCalendarMemberId;
import org.example.knockin.repository.room.RoommateCalendarMemberRepository;
import org.example.knockin.repository.room.row.DailyCalendarMemberRow;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateCalendarMemberServiceImpl {

    private final RoommateCalendarMemberRepository roommateCalendarMemberRepository;

    public List<RoommateCalendarMember> saveAll(RoommateCalendar calendar, List<Member> members) {
        List<RoommateCalendarMember> calendarMembers = members.stream()
                .map(member -> RoommateCalendarMember.of(calendar, member))
                .toList();
        return roommateCalendarMemberRepository.saveAll(calendarMembers);
    }

    public List<RoommateCalendarMember> findByRoommateCalendar(RoommateCalendar calendar) {
        return roommateCalendarMemberRepository.findByRoommateCalendar(calendar);
    }

    public void deleteByCalendarIdAndMemberId(Long roommateCalendarId, Long memberId) {
        RoommateCalendarMemberId id = RoommateCalendarMemberId.builder()
                .memberId(memberId)
                .roommateCalendarId(roommateCalendarId)
                .build();
        roommateCalendarMemberRepository.deleteById(id);
    }

    public List<DailyCalendarMemberRow> findDailyCalendarMembers(List<Long> calendarIds) {
        return roommateCalendarMemberRepository.findDailyCalendarMembers(calendarIds);
    }
}
