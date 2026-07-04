package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.Compatibility;
import org.example.knockin.dto.MyRoommateCardDto;
import org.example.knockin.entity.chat.ChattingRoom;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberPrivacy;
import org.example.knockin.entity.member.MemberPrivacyType;
import org.example.knockin.entity.room.MyRoommate;
import org.example.knockin.entity.room.RoommateMatchingRequired;
import org.example.knockin.entity.room.RoommateRequiredStatus;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("내 룸메이트 서비스")
class MyRoomMateServiceImplTest {

    @Mock
    private MyRoommateRepository myRoommateRepository;

    @Mock
    private BasicInformationRepository basicInformationRepository;

    @Mock
    private RoommateScoreRepository roommateScoreRepository;

    @Mock
    private RoommateScoreService roommateScoreService;

    @Mock
    private MemberPrivacyServiceImpl memberPrivacyService;

    @InjectMocks
    private MyRoomMateServiceImpl myRoomMateService;

    @Test
    @DisplayName("룸메이트가 존재하면 true를 반환한다")
    void isExistRoomMateTest() {
        // given
        Member member = mock(Member.class);
        given(myRoommateRepository.isExistRoomMate(member)).willReturn(true);

        // when
        boolean result = myRoomMateService.isExistRoomMate(member);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("내 룸메이트가 있으면 상대 기본 정보와 채팅방과 궁합 점수를 반환한다")
    void findMyRoommateReturnsOpponentInfoChatRoomAndScore() {
        // Given
        Long memberId = 1L;
        Long opponentId = 2L;
        LocalDate opponentBirth = LocalDate.of(2000, 1, 1);
        MyRoommate myRoommate = myRoommate(10L, memberId, opponentId, 100L);
        List<RoommateScore> roommateScores = List.of(RoommateScore.builder().score(80).build());
        ChattingRoomBasicInfoRow basicInfoRow = new ChattingRoomBasicInfoRow(
                opponentId,
                "상대방",
                opponentBirth,
                Gender.FEMALE,
                "opponent-profile.jpg"
        );

        when(myRoommateRepository.findWithRequiredByMemberId(memberId)).thenReturn(Optional.of(myRoommate));
        when(basicInformationRepository.findChattingRoomBasicInfoRow(opponentId)).thenReturn(Optional.of(basicInfoRow));
        when(roommateScoreRepository.findWithScoreDetailsByMyRoommateId(10L)).thenReturn(roommateScores);
        when(roommateScoreService.calculateRoommateCompatibility(memberId, roommateScores))
                .thenReturn(new Compatibility(92, List.of()));

        // When
        MyRoommateCardDto.Response response = myRoomMateService.findMyRoommate(memberId);

        // Then
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getChatRoomId()).isEqualTo(100L);
        assertThat(response.getScore()).isEqualTo(92);
        assertThat(response.getMyRoommateInfo().getMemberId()).isEqualTo(opponentId);
        assertThat(response.getMyRoommateInfo().getMemberName()).isEqualTo("상대방");
        assertThat(response.getMyRoommateInfo().getMemberAge()).isEqualTo(DateUtils.calculateAge(opponentBirth));
        assertThat(response.getMyRoommateInfo().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getMyRoommateInfo().getMemberProfileImageUrl()).isEqualTo("opponent-profile.jpg");
        verify(basicInformationRepository).findChattingRoomBasicInfoRow(opponentId);
        verify(roommateScoreRepository).findWithScoreDetailsByMyRoommateId(10L);
        verify(roommateScoreService).calculateRoommateCompatibility(memberId, roommateScores);
    }

    @Test
    @DisplayName("내 룸메이트가 없으면 룸메이트 없음 예외를 던진다")
    void findMyRoommateThrowsWhenMyRoommateDoesNotExist() {
        // Given
        Long memberId = 1L;
        when(myRoommateRepository.findWithRequiredByMemberId(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> myRoomMateService.findMyRoommate(memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MyRoommateErrorCode.NOT_FOUND));
        verifyNoInteractions(basicInformationRepository, roommateScoreRepository, roommateScoreService);
    }

    @Test
    @DisplayName("상대 회원의 기본 정보가 없으면 기본 정보 없음 예외를 던진다")
    void findMyRoommateThrowsWhenOpponentBasicInformationDoesNotExist() {
        // Given
        Long memberId = 1L;
        Long opponentId = 2L;
        MyRoommate myRoommate = myRoommate(10L, memberId, opponentId, 100L);
        when(myRoommateRepository.findWithRequiredByMemberId(memberId)).thenReturn(Optional.of(myRoommate));
        when(basicInformationRepository.findChattingRoomBasicInfoRow(opponentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> myRoomMateService.findMyRoommate(memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.BASIC_INFO_NOT_FOUND));
        verifyNoInteractions(roommateScoreRepository, roommateScoreService);
    }

    @Test
    @DisplayName("내 룸메이트를 삭제하면 룸메이트를 삭제 처리하고 공개범위를 공개로 변경한다")
    void deleteMyRoommateSoftDeletesMyRoommateAndChangesPrivacyPublic() {
        // Given
        Long myRoommateId = 10L;
        Long memberId = 1L;
        MyRoommate myRoommate = myRoommate(myRoommateId, memberId, 2L, 100L);
        MemberPrivacy memberPrivacy = MemberPrivacy.builder()
                .type(MemberPrivacyType.PRIVATE)
                .build();

        when(myRoommateRepository.findWithRequiredByMemberId(memberId)).thenReturn(Optional.of(myRoommate));
        when(memberPrivacyService.findByMemberId(memberId)).thenReturn(List.of(memberPrivacy));

        // When
        org.example.knockin.dto.MyRoommateDto.Response response = myRoomMateService.deleteMyRoommate(myRoommateId, memberId);

        // Then
        assertThat(myRoommate.getIsDeleted()).isTrue();
        assertThat(memberPrivacy.getType()).isEqualTo(MemberPrivacyType.PUBLIC);
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(memberPrivacyService).findByMemberId(memberId);
    }

    @Test
    @DisplayName("삭제하려는 룸메이트 ID가 내 룸메이트와 다르면 삭제 처리와 공개범위 변경을 하지 않는다")
    void deleteMyRoommateRejectsDifferentMyRoommateId() {
        // Given
        Long memberId = 1L;
        MyRoommate myRoommate = myRoommate(10L, memberId, 2L, 100L);
        when(myRoommateRepository.findWithRequiredByMemberId(memberId)).thenReturn(Optional.of(myRoommate));

        // When & Then
        assertThatThrownBy(() -> myRoomMateService.deleteMyRoommate(999L, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.BAD_REQUEST));
        assertThat(myRoommate.getIsDeleted()).isFalse();
        verifyNoInteractions(memberPrivacyService);
    }

    @Test
    @DisplayName("삭제할 내 룸메이트가 없으면 룸메이트 없음 예외를 던지고 공개범위를 변경하지 않는다")
    void deleteMyRoommateThrowsWhenMyRoommateDoesNotExist() {
        // Given
        Long memberId = 1L;
        when(myRoommateRepository.findWithRequiredByMemberId(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> myRoomMateService.deleteMyRoommate(10L, memberId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MyRoommateErrorCode.NOT_FOUND));
        verifyNoInteractions(memberPrivacyService);
    }

    private MyRoommate myRoommate(Long myRoommateId, Long requesterId, Long requesteeId, Long chatRoomId) {
        Member requester = Member.builder().id(requesterId).build();
        Member requestee = Member.builder().id(requesteeId).build();
        ChattingRoom chattingRoom = ChattingRoom.builder()
                .id(chatRoomId)
                .build();
        RoommateMatchingRequired matchingRequired = RoommateMatchingRequired.builder()
                .requester(requester)
                .requestee(requestee)
                .chattingRoom(chattingRoom)
                .status(RoommateRequiredStatus.ACCEPTED)
                .build();

        return MyRoommate.builder()
                .id(myRoommateId)
                .roommateMatchingRequired(matchingRequired)
                .isDeleted(false)
                .build();
    }
}
