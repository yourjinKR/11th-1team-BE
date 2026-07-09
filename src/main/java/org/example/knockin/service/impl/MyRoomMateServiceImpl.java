package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.CalendarDto;
import org.example.knockin.dto.CalendarEditDto;
import org.example.knockin.dto.Compatibility;
import org.example.knockin.dto.HouseRuleDetailDto;
import org.example.knockin.dto.HouseRuleDto;
import org.example.knockin.dto.HouseRuleListDto;
import org.example.knockin.dto.MyRoommateCardDto;
import org.example.knockin.dto.MyRoommateCardDto.Response.MyRoommateInfo;
import org.example.knockin.dto.MyRoommateDailyCalendarListDto;
import org.example.knockin.dto.MyRoommateDto;
import org.example.knockin.dto.MyRoommateMonthlyCalendarListDto;
import org.example.knockin.dto.RepeatCalendarDto;
import org.example.knockin.dto.RepeatCalendarModifyDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberPrivacy;
import org.example.knockin.entity.member.MemberPrivacyType;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateScore;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.CommonErrorCode;
import org.example.knockin.exception.MyRoommateErrorCode;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.example.knockin.service.RoommateScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyRoomMateServiceImpl {
    private final MyRoommateRepository myRoommateRepository;
    private final RoommateScoreService roommateScoreService;
    private final MemberPrivacyServiceImpl memberPrivacyService;
    private final BasicInformationServiceImpl basicInformationService;
    private final MyRoommateScoreServiceImpl myRoommateScoreService;
    private final CalendarServiceImpl calendarService;
    private final HouseRuleServiceImpl houseRuleService;

    public boolean isExistRoomMate(Member member) {
        return myRoommateRepository.isExistRoomMate(member);
    }

    @Transactional
    public MyRoommate save(RoommateMatchingRequired roommateMatchingRequired) {
        MyRoommate myRoommate = MyRoommate.builder()
                .roommateMatchingRequired(roommateMatchingRequired)
                .isDeleted(false)
                .build();
        return myRoommateRepository.save(myRoommate);
    }

    @Transactional(readOnly = true)
    public MyRoommateCardDto.Response findMyRoommate(Long memberId) {
        MyRoommate myRoommate = myRoommateRepository.findWithRequiredByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        Long requesterId = roommateMatchingRequired.getRequester().getId();
        Long requesteeId = roommateMatchingRequired.getRequestee().getId();

        Long opponentId = getOpponentId(memberId, requesterId, requesteeId);
        ChattingRoomBasicInfoRow basicInfoRow = basicInformationService.findChattingRoomBasicInfoRowByMemberId(opponentId);
        MyRoommateInfo myRoommateInfo = toMyRoommateInfo(basicInfoRow);

        Long myRoommateId = myRoommate.getId();
        List<RoommateScore> roommateScores = myRoommateScoreService.findByRoommateId(myRoommateId);
        Compatibility compatibility = roommateScoreService.calculateRoommateCompatibility(memberId, roommateScores);
        Integer totalScore = compatibility.getTotalScore();

        Long chatRoomId = roommateMatchingRequired.getChattingRoom().getId();

        return MyRoommateCardDto.Response.builder()
                .id(myRoommateId)
                .myRoommateInfo(myRoommateInfo)
                .chatRoomId(chatRoomId)
                .score(totalScore)
                .build();
    }

    private Long getOpponentId(Long myId, Long memberId1, Long memberId2) {
        if (Objects.equals(myId, memberId1)) return memberId2;
        if (Objects.equals(myId, memberId2)) return memberId1;
        throw new BusinessException(CommonErrorCode.BAD_REQUEST);
    }

    private MyRoommateCardDto.Response.MyRoommateInfo toMyRoommateInfo(ChattingRoomBasicInfoRow row) {
        return MyRoommateCardDto.Response.MyRoommateInfo.builder()
                .memberId(row.memberId())
                .memberName(row.name())
                .memberAge(DateUtils.calculateAge(row.birth()))
                .gender(row.gender())
                .memberProfileImageUrl(row.profileImageUrl())
                .build();
    }

    @Transactional
    public MyRoommateDto.Response deleteMyRoommate(Long id, Long memberId) {
        MyRoommate myRoommate = myRoommateRepository.findWithRequiredByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        if (!validateMyRoommate(id, memberId, myRoommate)) throw new BusinessException(CommonErrorCode.BAD_REQUEST);

        myRoommate.softDelete();

        MemberPrivacy memberPrivacy = memberPrivacyService.findByMemberId(memberId).getFirst();
        memberPrivacy.changeState(MemberPrivacyType.PUBLIC);

        return MyRoommateDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    private boolean validateMyRoommate(Long id, Long memberId, MyRoommate myRoommate) {
        if (!Objects.equals(id, myRoommate.getId())) return false;

        Long requesterId = myRoommate.getRoommateMatchingRequired().getRequester().getId();
        Long requesteeId = myRoommate.getRoommateMatchingRequired().getRequestee().getId();

        return Objects.equals(memberId, requesterId) || (Objects.equals(memberId, requesteeId));
    }

    private MyRoommate findWithRequiredAndMembersByMemberId(Long memberId) {
        return myRoommateRepository.findWithRequiredAndMembersByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
    }

    @Transactional
    public HouseRuleDto.Response saveHouseRule(HouseRuleDto.Request request, Long memberId) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return houseRuleService.save(myRoommate, request, memberId);
    }

    @Transactional(readOnly = true)
    public List<HouseRuleListDto.Response> findHouseRuleList(Long memberId) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return houseRuleService.findList(myRoommate);
    }

    @Transactional
    public HouseRuleDetailDto.Response findHouseRuleDetail(Long memberId, Long houseRuleId) {
        return houseRuleService.findDetail(memberId, houseRuleId);
    }

    @Transactional
    public HouseRuleDto.Response modifyHouseRule(Long memberId, Long houseRuleId, HouseRuleDto.Request request) {
        return houseRuleService.modify(memberId, houseRuleId, request);
    }

    @Transactional
    public HouseRuleDto.Response deleteHouseRule(Long memberId, Long houseRuleId) {
        return houseRuleService.delete(memberId, houseRuleId);
    }

    @Transactional
    public CalendarDto.Response saveBasicCalendar(Long memberId, CalendarDto.Request request) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return calendarService.saveBasic(memberId, myRoommate, request);
    }

    @Transactional
    public RepeatCalendarDto.Response saveRepeatCalendar(Long memberId, RepeatCalendarDto.Request request) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return calendarService.saveRepeat(memberId, myRoommate, request);
    }

    @Transactional
    public MyRoommateDailyCalendarListDto.Response findDailyCalendarList(Long memberId, Integer year, Integer month, Integer day) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return calendarService.findDailyList(myRoommate, year, month, day);
    }

    @Transactional
    public MyRoommateMonthlyCalendarListDto.Response findMyMonthlyCalendarList(Long memberId, Integer year, Integer month) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return calendarService.findMyMonthlyList(myRoommate, year, month);
    }

    public List<String> findCategoryNames() {
        return calendarService.findCategoryNames();
    }

    @Transactional
    public CalendarEditDto.Response getRoommateCalendarEditForm(Long memberId) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return calendarService.getEditForm(memberId, myRoommate);
    }

    @Transactional
    public CalendarDto.Response modifyCalendar(Long memberId, Long calendarId, CalendarDto.Request request) {
        return calendarService.modifyCalendar(memberId, calendarId, request);
    }

    @Transactional
    public RepeatCalendarModifyDto.Response modifyRepeatCalendar(Long memberId, Long calendarId, RepeatCalendarModifyDto.Request request) {
        MyRoommate myRoommate = findWithRequiredAndMembersByMemberId(memberId);
        return calendarService.modifyRepeat(memberId, calendarId, myRoommate, request);
    }

    @Transactional
    public CalendarDto.Response deleteCalendar(Long memberId, Long calendarId) {
        return calendarService.delete(memberId, calendarId);
    }
}
