package org.example.knockin.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.Compatibility;
import org.example.knockin.dto.MyRoommateCardDto;
import org.example.knockin.dto.MyRoommateCardDto.Response.MyRoommateInfo;
import org.example.knockin.dto.MyRoommateDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberPrivacy;
import org.example.knockin.entity.member.MemberPrivacyType;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateScore;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.CommonErrorCode;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.exception.MyRoommateErrorCode;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.member.row.ChattingRoomBasicInfoRow;
import org.example.knockin.repository.room.MyRoommateRepository;
import org.example.knockin.repository.room.RoommateScoreRepository;
import org.example.knockin.service.RoommateScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyRoomMateServiceImpl {
    private final MyRoommateRepository myRoommateRepository;
    private final BasicInformationRepository basicInformationRepository;
    private final RoommateScoreRepository roommateScoreRepository;
    private final RoommateScoreService roommateScoreService;
    private final MemberPrivacyServiceImpl memberPrivacyService;

    public boolean isExistRoomMate(Member member) {
        return myRoommateRepository.isExistRoomMate(member);
    }

    @Transactional(readOnly = true)
    public MyRoommateCardDto.Response findMyRoommate(Long memberId) {
        MyRoommate myRoommate = myRoommateRepository.findWithFetchedByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
        RoommateMatchingRequired roommateMatchingRequired = myRoommate.getRoommateMatchingRequired();
        Long requesterId = roommateMatchingRequired.getRequester().getId();
        Long requesteeId = roommateMatchingRequired.getRequestee().getId();

        Long opponentId = getOpponentId(memberId, requesterId, requesteeId);
        ChattingRoomBasicInfoRow basicInfoRow = basicInformationRepository.findChattingRoomBasicInfoRow(opponentId).orElseThrow(() -> new BusinessException(MemberErrorCode.BASIC_INFO_NOT_FOUND));
        MyRoommateInfo myRoommateInfo = toMyRoommateInfo(basicInfoRow);

        Long myRoommateId = myRoommate.getId();
        List<RoommateScore> roommateScores = roommateScoreRepository.findWithScoreDetailsByMyRoommateId(myRoommateId);
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
        MyRoommate myRoommate = myRoommateRepository.findWithFetchedByMemberId(memberId).orElseThrow(() -> new BusinessException(MyRoommateErrorCode.NOT_FOUND));
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
}
