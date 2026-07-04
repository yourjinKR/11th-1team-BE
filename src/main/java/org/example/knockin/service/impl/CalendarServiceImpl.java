package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.CalendarDto;
import org.example.knockin.dto.CalendarDto.CalendarInfoDto;
import org.example.knockin.dto.CalendarEditDto;
import org.example.knockin.dto.CalendarEditDto.MemberInfo;
import org.example.knockin.dto.RepeatCalendarDto;
import org.example.knockin.dto.RepeatCalendarDto.RepeatCalendarInfo;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RepeatRoommateCalendar;
import org.example.knockin.entity.room.RepeatType;
import org.example.knockin.entity.room.RoommateCalendar;
import org.example.knockin.entity.room.RoommateCalendarCategory;
import org.example.knockin.entity.room.RoommateCalendarMember;
import org.example.knockin.entity.room.RoommateCalendarMemberId;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MyRoommateErrorCode;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.member.row.MemberWithNameRow;
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
    private final MemberRepository memberRepository;

    @Transactional
    public CalendarDto.Response saveBasicCalendar(Long memberId, CalendarDto.Request request) {
        MyRoommate myRoommate = myRoommateRepository.findWithRequiredAndMembersByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        RoommateCalendarCategory roommateCalendarCategory = saveCalendarCategory(request.getCategoryName());
        RoommateCalendar roommateCalendar = saveCalendar(memberId, myRoommate, roommateCalendarCategory, request.getCalendar());
        saveCalendarMembers(roommateCalendar, roommateMatchingRequired, request.getMemberIds());
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

    private List<RoommateCalendarMember> saveCalendarMembers(RoommateCalendar calendar, RoommateMatchingRequired required, List<Long> memberIds) {
        List<Member> members = pickRoommateMembers(memberIds, required);
        return saveCalendarMembers(calendar, members);
    }

    private List<RoommateCalendarMember> saveCalendarMembers(RoommateCalendar calendar, List<Member> members) {
        List<RoommateCalendarMember> calendarMembers = members.stream()
                .map(member -> RoommateCalendarMember.of(calendar, member))
                .toList();
        return roommateCalendarMemberRepository.saveAll(calendarMembers);
    }

    private List<Member> pickRoommateMembers(List<Long> memberIds, RoommateMatchingRequired required) {
        Member requester = required.getRequester();
        Member requestee = required.getRequestee();

        Map<Long, Member> roommateMembers = Map.of(
                requester.getId(), requester,
                requestee.getId(), requestee
        );

        return memberIds.stream()
                .map(memberId -> {
                    Member member = roommateMembers.get(memberId);
                    if (member == null) throw new BusinessException(MyRoommateErrorCode.CALENDER_ACCESS_DENIED);
                    return member;
                })
                .toList();
    }

    private Member pickMe(Long memberId, RoommateMatchingRequired roommateMatchingRequired) {
        Member requester = roommateMatchingRequired.getRequester();
        Member requestee = roommateMatchingRequired.getRequestee();
        return Objects.equals(requester.getId(), memberId) ? requester : requestee;
    }

    @Transactional
    public RepeatCalendarDto.Response saveRepeatCalendar(Long memberId, RepeatCalendarDto.Request request) {
        MyRoommate myRoommate = myRoommateRepository.findWithRequiredAndMembersByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        RoommateCalendarCategory roommateCalendarCategory = saveCalendarCategory(request.getCategoryName());
        RoommateCalendar roommateCalendar = saveCalendar(memberId, myRoommate, roommateCalendarCategory, request.getCalendar());
        saveCalendarMembers(roommateCalendar, roommateMatchingRequired, request.getMemberIds());
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

    @Transactional
    public CalendarEditDto.Response getRoommateEditForm(Long memberId) {
        MyRoommate myRoommate = myRoommateRepository.findWithRequiredAndMembersByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        List<MemberInfo> memberInfos = findAllMemberInfo(memberId, roommateMatchingRequired);
        List<String> categoryNames = findCategoryNames();
        return CalendarEditDto.Response.builder()
                .repeatType(List.of(RepeatType.values()))
                .members(memberInfos)
                .categoryNames(categoryNames)
                .build();
    }

    private List<MemberInfo> findAllMemberInfo(Long myId, RoommateMatchingRequired roommateMatchingRequired) {
        List<Long> ids = List.of(roommateMatchingRequired.getRequester().getId(), roommateMatchingRequired.getRequestee().getId());
        List<MemberWithNameRow> rows = memberRepository.findAllWithNameRowById(ids);
        return rows.stream()
                .map(row -> toEditDto(myId, row))
                .toList();
    }

    private MemberInfo toEditDto(Long myId, MemberWithNameRow row) {
        return MemberInfo.builder()
                .memberId(row.id())
                .name(row.name())
                .isMe(Objects.equals(myId, row.id()))
                .build();
    }

    // TODO: 스펙 확정 후 수정
    public List<String> findCategoryNames() {
        return List.of("청소", "공과금", "기타");
    }

    @Transactional
    public CalendarDto.Response modifyCalendar(Long memberId, Long calendarId, CalendarDto.Request request) {
        RoommateCalendar roommateCalendar = roommateCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new BusinessException(MyRoommateErrorCode.CALENDER_NOT_FOUND));

        if (!roommateCalendar.isOwner(memberId)) {
            throw new BusinessException(MyRoommateErrorCode.CALENDER_ACCESS_DENIED);
        }

        roommateCalendar.modify(request.getCalendar());
        modifyCalendarCategory(roommateCalendar, request.getCategoryName());
        modifyCalendarMember(roommateCalendar, request.getMemberIds());

        return CalendarDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    // TODO: 스펙 확정 후 수정
    private void modifyCalendarCategory(RoommateCalendar calendar, String categoryName) {
        RoommateCalendarCategory roommateCalendarCategory = calendar.getRoommateCalendarCategory();
        roommateCalendarCategory.rename(categoryName);
    }

    private void modifyCalendarMember(RoommateCalendar calendar, List<Long> requestMemberIds) {
        List<Member> requestedMembers = pickRoommateMembers(
                requestMemberIds,
                calendar.getMyRoommate().getRoommateMatchingRequired()
        );

        List<RoommateCalendarMember> calendarMembers = roommateCalendarMemberRepository.findByRoommateCalendar(calendar);
        List<Long> existingMemberIds = calendarMembers.stream().map(RoommateCalendarMember::getMemberId).toList();

        Set<Long> existingIdSet = new HashSet<>(existingMemberIds);
        Set<Long> requestIdSet = new HashSet<>(requestMemberIds);

        List<Member> membersToAdd = requestedMembers.stream()
                .filter(member -> !existingIdSet.contains(member.getId()))
                .toList();

        List<Long> idsToRemove = existingMemberIds.stream()
                .filter(id -> !requestIdSet.contains(id))
                .toList();

        saveCalendarMembers(calendar, membersToAdd);
        idsToRemove.forEach(memberId -> deleteCalendarMember(calendar.getId(), memberId));
    }

    private void deleteCalendarMember(Long roommateCalendarId, Long memberId) {
        RoommateCalendarMemberId id = RoommateCalendarMemberId.builder()
                .memberId(memberId)
                .roommateCalendarId(roommateCalendarId)
                .build();
        roommateCalendarMemberRepository.deleteById(id);
    }
}
