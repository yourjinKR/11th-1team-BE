package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.HouseRuleDetailDto;
import org.example.knockin.dto.HouseRuleDto;
import org.example.knockin.dto.HouseRuleListDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateHouseRule;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.MyRoommateErrorCode;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.example.knockin.repository.room.RoommateHouseRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HouseRuleServiceImpl {

    private final MyRoommateRepository myRoommateRepository;
    private final MemberRepository memberRepository;
    private final RoommateHouseRuleRepository roommateHouseRuleRepository;

    @Transactional
    public HouseRuleDto.Response saveHouseRule(HouseRuleDto.Request request, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        MyRoommate myRoommate = myRoommateRepository.findWithFetchedByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));

        RoommateHouseRule roommateHouseRule = RoommateHouseRule.builder()
                .member(member)
                .myRoommate(myRoommate)
                .title(request.getTitle())
                .contents(request.getContents())
                .build();
        roommateHouseRuleRepository.save(roommateHouseRule);
        return HouseRuleDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional(readOnly = true)
    public List<HouseRuleListDto.Response> findHouseRuleList(Long memberId) {
        MyRoommate myRoommate = myRoommateRepository.findWithFetchedByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        List<RoommateHouseRule> myRoommateRules = roommateHouseRuleRepository.findByMyRoommateIdAndIsDeleted(myRoommate.getId(), false);
        return myRoommateRules.stream().map(this::toListDto).toList();
    }

    private HouseRuleListDto.Response toListDto(RoommateHouseRule roommateHouseRule) {
        return HouseRuleListDto.Response.builder()
                .id(roommateHouseRule.getId())
                .title(roommateHouseRule.getTitle())
                .contents(roommateHouseRule.getContents())
                .build();
    }

    @Transactional(readOnly = true)
    public HouseRuleDetailDto.Response findHouseRuleDetail(Long memberId, Long houseRuleId) {
        RoommateHouseRule roommateHouseRule = roommateHouseRuleRepository.findWithFetchedById(houseRuleId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.HOUSE_RULE_NOT_FOUND));
        validateHouseRule(memberId, roommateHouseRule);

        return HouseRuleDetailDto.Response.builder()
                .id(roommateHouseRule.getId())
                .title(roommateHouseRule.getTitle())
                .contents(roommateHouseRule.getContents())
                .build();
    }

    @Transactional
    public HouseRuleDto.Response modifyHouseRule(Long memberId, Long houseRuleId, HouseRuleDto.Request request) {
        RoommateHouseRule roommateHouseRule = roommateHouseRuleRepository.findWithFetchedById(houseRuleId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.HOUSE_RULE_NOT_FOUND));
        validateHouseRule(memberId, roommateHouseRule);
        roommateHouseRule.modify(request.getTitle(), request.getContents());
        return HouseRuleDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public HouseRuleDto.Response deleteHouseRule(Long memberId, Long houseRuleId) {
        RoommateHouseRule roommateHouseRule = roommateHouseRuleRepository.findWithFetchedById(houseRuleId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.HOUSE_RULE_NOT_FOUND));
        validateHouseRule(memberId, roommateHouseRule);
        roommateHouseRule.softDelete();
        return HouseRuleDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    private void validateHouseRule(Long memberId, RoommateHouseRule roommateHouseRule) {
        RoommateMatchingRequired roommateMatchingRequired = roommateHouseRule.getMyRoommate().getRoommateMatchingRequired();
        Long requesterId = roommateMatchingRequired.getRequester().getId();
        Long requesteeId = roommateMatchingRequired.getRequestee().getId();

        if (!Objects.equals(memberId, requesterId) && !Objects.equals(memberId, requesteeId))
            throw new BusinessException(MyRoommateErrorCode.HOUSE_RULE_ACCESS_DENIED);
    }
}
