package org.example.knockin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.knockin.dto.MatchDetailDto;
import org.example.knockin.dto.MatchListDto;
import org.example.knockin.entity.auth.AuthenticationType;
import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.room.RoomProfileType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.MemberErrorCode;
import org.example.knockin.global.util.DateUtils;
import org.example.knockin.repository.auth.AuthenticationRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.life.PreferenceConditionRepository;
import org.example.knockin.repository.life.PreferenceConditionWeightRepository;
import org.example.knockin.repository.life.row.MatchingLifestyleRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionRow;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionWeightRow;
import org.example.knockin.repository.member.MemberInterestRepository;
import org.example.knockin.repository.member.MemberRepository;
import org.example.knockin.repository.member.row.MatchingBasicInfoRow;
import org.example.knockin.repository.room.RoomOfferProfileRepository;
import org.example.knockin.repository.room.RoomSeekerProfileRepository;
import org.example.knockin.repository.room.row.MatchingOfferProfileRow;
import org.example.knockin.repository.room.row.MatchingSeekerProfileRow;
import org.example.knockin.repository.room.row.MatchingSeekerRegionRow;
import org.example.knockin.repository.room.row.MatchingSeekerRoomTypeRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;

@ExtendWith(MockitoExtension.class)
@DisplayName("룸메이트 매칭 서비스")
class RoommateMatchingServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberInterestRepository memberInterestRepository;

    @Mock
    private RoomSeekerProfileRepository roomSeekerProfileRepository;

    @Mock
    private RoomOfferProfileRepository roomOfferProfileRepository;

    @Mock
    private MemberLifePatternRepository memberLifePatternRepository;

    @Mock
    private PreferenceConditionRepository preferenceConditionRepository;

    @Mock
    private PreferenceConditionWeightRepository preferenceConditionWeightRepository;

    @Mock
    private AuthenticationRepository authenticationRepository;

    @InjectMocks
    private RoommateMatchingServiceImpl roommateMatchingService;

    @Test
    @DisplayName("조회 크기보다 후보가 많으면 요청 크기만 응답하고 다음 목록이 있음을 반환한다")
    void findMatchingListReturnsRequestedSizeAndHasNext() {
        // Given
        Long viewerId = 10L;
        MatchListDto.Request request = new MatchListDto.Request();
        request.setSize(2);
        request.setExcludeMemberIds(Arrays.asList(99L, null, 99L));

        LocalDate offerBirth = LocalDate.of(2000, 1, 1);
        LocalDate seekerBirth = LocalDate.of(1999, 5, 10);

        when(memberRepository.findMatchingBasicRow(List.of(99L, viewerId), 3))
                .thenReturn(List.of(
                        new MatchingBasicInfoRow(1L, "offer-profile.png", "오퍼", offerBirth, Gender.MALE, 101L, RoomProfileType.OFFER),
                        new MatchingBasicInfoRow(2L, "seeker-profile.png", "시커", seekerBirth, Gender.FEMALE, 102L, RoomProfileType.SEEKER),
                        new MatchingBasicInfoRow(3L, "next-profile.png", "다음", LocalDate.of(2001, 3, 3), Gender.MALE, 103L, RoomProfileType.OFFER)
                ));
        when(roomOfferProfileRepository.findAllOfferProfileByMemberIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(new MatchingOfferProfileRow(1L, 500, 45, "역삼동", "강남구", "서울특별시", "원룸")));
        when(roomSeekerProfileRepository.findAllSeekerProfileByMemberIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(new MatchingSeekerProfileRow(2L, 300, 1000, 30, 70)));
        when(roomSeekerProfileRepository.findAllSeekerRegionByMemberIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(new MatchingSeekerRegionRow(2L, "성수동", "성동구", "서울특별시")));
        when(roomSeekerProfileRepository.findAllSeekerRoomTypeByMemberIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(new MatchingSeekerRoomTypeRow(2L, "투룸")));
        when(memberLifePatternRepository.findAllLifestyleByMemberIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(
                        new MatchingLifestyleRow(1L, 11L, "청결", "4", "깔끔함", LifePatternType.SCALE),
                        new MatchingLifestyleRow(2L, 12L, "흡연", "2", "비흡연자", LifePatternType.SINGLE_CHOICE)
                ));
        when(preferenceConditionRepository.findAllPreferenceConditionByMemberIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(new MatchingPreferenceConditionRow(2L, 21L, "소음", "1", "조용함", LifePatternType.SCALE)));
        when(preferenceConditionWeightRepository.findAllPreferenceConditionWeightByMemberIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(new MatchingPreferenceConditionWeightRow(2L, 31L, "흡연")));
        when(memberInterestRepository.findActiveReceiverIdsBySenderIdAndReceiverIds(viewerId, List.of(1L, 2L)))
                .thenReturn(List.of(2L));

        // When
        Slice<MatchListDto.Response> response = roommateMatchingService.findMatchingList(viewerId, request);

        // Then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.getContent()).hasSize(2);

        MatchListDto.Response offer = response.getContent().get(0);
        assertThat(offer.getMemberId()).isEqualTo(1L);
        assertThat(offer.getMemberName()).isEqualTo("오퍼");
        assertThat(offer.getMemberAge()).isEqualTo(DateUtils.calculateAge(offerBirth));
        assertThat(offer.getIsLike()).isFalse();
        assertThat(offer.getScore()).isNull();
        assertThat(offer.getOfferProfile().getDeposit()).isEqualTo(500);
        assertThat(offer.getOfferProfile().getMonthlyRent()).isEqualTo(45);
        assertThat(offer.getOfferProfile().getRegionFullName()).isEqualTo("서울특별시 강남구 역삼동");
        assertThat(offer.getOfferProfile().getRoomTypeName()).isEqualTo("원룸");
        assertThat(offer.getSeekerProfile()).isNull();
        assertThat(offer.getLifeStyles()).extracting(MatchListDto.Lifestyle::getName).containsExactly("청결");

        MatchListDto.Response seeker = response.getContent().get(1);
        assertThat(seeker.getMemberId()).isEqualTo(2L);
        assertThat(seeker.getMemberName()).isEqualTo("시커");
        assertThat(seeker.getMemberAge()).isEqualTo(DateUtils.calculateAge(seekerBirth));
        assertThat(seeker.getIsLike()).isTrue();
        assertThat(seeker.getOfferProfile()).isNull();
        assertThat(seeker.getSeekerProfile().getMinDeposit()).isEqualTo(300);
        assertThat(seeker.getSeekerProfile().getMaxDeposit()).isEqualTo(1000);
        assertThat(seeker.getSeekerProfile().getRoomTypeNames()).containsExactly("투룸");
        assertThat(seeker.getSeekerProfile().getRegionFullNames()).containsExactly("서울특별시 성동구 성수동");
        assertThat(seeker.getConditions()).extracting(MatchListDto.Condition::getName).containsExactly("소음");
        assertThat(seeker.getConditionWeights()).extracting(MatchListDto.ConditionWeight::getName).containsExactly("흡연");

        verify(memberRepository).findMatchingBasicRow(List.of(99L, viewerId), 3);
        verify(roomOfferProfileRepository).findAllOfferProfileByMemberIdIn(List.of(1L, 2L));
        verify(roomSeekerProfileRepository).findAllSeekerProfileByMemberIdIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("후보 회원이 없으면 추가 조회 없이 빈 Slice를 반환한다")
    void findMatchingListReturnsEmptySliceWithoutLoadingDetails() {
        // Given
        Long viewerId = 10L;
        MatchListDto.Request request = new MatchListDto.Request();
        request.setSize(20);
        when(memberRepository.findMatchingBasicRow(List.of(viewerId), 21)).thenReturn(List.of());

        // When
        Slice<MatchListDto.Response> response = roommateMatchingService.findMatchingList(viewerId, request);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        verify(memberRepository).findMatchingBasicRow(List.of(viewerId), 21);
        verifyNoInteractions(
                roomOfferProfileRepository,
                roomSeekerProfileRepository,
                memberLifePatternRepository,
                preferenceConditionRepository,
                preferenceConditionWeightRepository,
                memberInterestRepository
        );
    }

    @Test
    @DisplayName("비로그인 사용자는 관심 조회를 하지 않고 좋아요 여부를 false로 반환한다")
    void findMatchingListDoesNotLoadLikesForAnonymousUser() {
        // Given
        MatchListDto.Request request = new MatchListDto.Request();
        request.setSize(1);

        when(memberRepository.findMatchingBasicRow(List.of(), 2))
                .thenReturn(List.of(new MatchingBasicInfoRow(
                        1L,
                        "offer-profile.png",
                        "오퍼",
                        LocalDate.of(2000, 1, 1),
                        Gender.MALE,
                        101L,
                        RoomProfileType.OFFER
                )));
        when(roomOfferProfileRepository.findAllOfferProfileByMemberIdIn(List.of(1L)))
                .thenReturn(List.of(new MatchingOfferProfileRow(1L, 500, 45, "역삼동", "강남구", "서울특별시", "원룸")));
        when(roomSeekerProfileRepository.findAllSeekerProfileByMemberIdIn(List.of(1L))).thenReturn(List.of());
        when(roomSeekerProfileRepository.findAllSeekerRegionByMemberIdIn(List.of(1L))).thenReturn(List.of());
        when(roomSeekerProfileRepository.findAllSeekerRoomTypeByMemberIdIn(List.of(1L))).thenReturn(List.of());
        when(memberLifePatternRepository.findAllLifestyleByMemberIdIn(List.of(1L))).thenReturn(List.of());
        when(preferenceConditionRepository.findAllPreferenceConditionByMemberIdIn(List.of(1L))).thenReturn(List.of());
        when(preferenceConditionWeightRepository.findAllPreferenceConditionWeightByMemberIdIn(List.of(1L))).thenReturn(List.of());

        // When
        Slice<MatchListDto.Response> response = roommateMatchingService.findMatchingList(null, request);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getIsLike()).isFalse();
        assertThat(response.hasNext()).isFalse();
        verify(memberInterestRepository, never()).findActiveReceiverIdsBySenderIdAndReceiverIds(any(), anyList());
    }

    @Test
    @DisplayName("방이 있는 회원의 상세 조회는 방 있음 프로필과 인증 정보를 반환한다")
    void findMatchingDetailReturnsOfferProfileAndAuthentications() {
        // Given
        Long targetMemberId = 1L;
        Long requesterId = 10L;
        LocalDate birth = LocalDate.of(2000, 1, 1);

        when(memberRepository.findMatchingBasicRowById(targetMemberId))
                .thenReturn(Optional.of(new MatchingBasicInfoRow(
                        targetMemberId,
                        "offer-profile.png",
                        "오퍼",
                        birth,
                        Gender.MALE,
                        101L,
                        RoomProfileType.OFFER
                )));
        when(authenticationRepository.getAcceptedAuthenticationTypeByMemberId(targetMemberId))
                .thenReturn(List.of(AuthenticationType.STUDENT));
        when(roomOfferProfileRepository.findAllOfferProfileByMemberIdIn(List.of(targetMemberId)))
                .thenReturn(List.of(new MatchingOfferProfileRow(targetMemberId, 500, 45, "역삼동", "강남구", "서울특별시", "원룸")));
        when(memberLifePatternRepository.findAllLifestyleByMemberIdIn(List.of(targetMemberId)))
                .thenReturn(List.of(new MatchingLifestyleRow(targetMemberId, 11L, "청결", "4", "깔끔함", LifePatternType.SCALE)));
        when(preferenceConditionRepository.findAllPreferenceConditionByMemberIdIn(List.of(targetMemberId)))
                .thenReturn(List.of(new MatchingPreferenceConditionRow(targetMemberId, 21L, "소음", "1", "조용함", LifePatternType.SCALE)));
        when(preferenceConditionWeightRepository.findAllPreferenceConditionWeightByMemberIdIn(List.of(targetMemberId)))
                .thenReturn(List.of(new MatchingPreferenceConditionWeightRow(targetMemberId, 31L, "흡연")));
        when(memberInterestRepository.existsBySenderIdAndReceiverId(requesterId, targetMemberId)).thenReturn(true);

        // When
        MatchDetailDto.Response response = roommateMatchingService.findMatchingDetail(targetMemberId, requesterId);

        // Then
        assertThat(response.getMemberId()).isEqualTo(targetMemberId);
        assertThat(response.getMemberProfileImageUrl()).isEqualTo("offer-profile.png");
        assertThat(response.getMemberName()).isEqualTo("오퍼");
        assertThat(response.getMemberAge()).isEqualTo(DateUtils.calculateAge(birth));
        assertThat(response.getGender()).isEqualTo(Gender.MALE);
        assertThat(response.getIsLike()).isTrue();
        assertThat(response.getRoomProfileType()).isEqualTo(RoomProfileType.OFFER);
        assertThat(response.getOfferProfile().getDeposit()).isEqualTo(500);
        assertThat(response.getOfferProfile().getMonthlyRent()).isEqualTo(45);
        assertThat(response.getOfferProfile().getRegionFullName()).isEqualTo("서울특별시 강남구 역삼동");
        assertThat(response.getOfferProfile().getRoomTypeName()).isEqualTo("원룸");
        assertThat(response.getSeekerProfile()).isNull();
        assertThat(response.getLifeStyles()).extracting(MatchListDto.Lifestyle::getName).containsExactly("청결");
        assertThat(response.getConditions()).extracting(MatchListDto.Condition::getName).containsExactly("소음");
        assertThat(response.getConditionWeights()).extracting(MatchListDto.ConditionWeight::getName).containsExactly("흡연");
        assertThat(response.getAuthentications()).containsExactly(AuthenticationType.STUDENT);
        assertThat(response.getCompatibility()).isNull();
        verifyNoInteractions(roomSeekerProfileRepository);
    }

    @Test
    @DisplayName("방이 없는 회원의 상세 조회는 방 없음 프로필만 반환하고 비로그인은 관심 조회를 하지 않는다")
    void findMatchingDetailReturnsSeekerProfileForAnonymousUser() {
        // Given
        Long targetMemberId = 2L;
        LocalDate birth = LocalDate.of(1999, 5, 10);

        when(memberRepository.findMatchingBasicRowById(targetMemberId))
                .thenReturn(Optional.of(new MatchingBasicInfoRow(
                        targetMemberId,
                        "seeker-profile.png",
                        "시커",
                        birth,
                        Gender.FEMALE,
                        102L,
                        RoomProfileType.SEEKER
                )));
        when(authenticationRepository.getAcceptedAuthenticationTypeByMemberId(targetMemberId)).thenReturn(List.of());
        when(roomSeekerProfileRepository.findAllSeekerProfileByMemberIdIn(List.of(targetMemberId)))
                .thenReturn(List.of(new MatchingSeekerProfileRow(targetMemberId, 300, 1000, 30, 70)));
        when(roomSeekerProfileRepository.findAllSeekerRegionByMemberIdIn(List.of(targetMemberId)))
                .thenReturn(List.of(new MatchingSeekerRegionRow(targetMemberId, "성수동", "성동구", "서울특별시")));
        when(roomSeekerProfileRepository.findAllSeekerRoomTypeByMemberIdIn(List.of(targetMemberId)))
                .thenReturn(List.of(new MatchingSeekerRoomTypeRow(targetMemberId, "투룸")));
        when(memberLifePatternRepository.findAllLifestyleByMemberIdIn(List.of(targetMemberId))).thenReturn(List.of());
        when(preferenceConditionRepository.findAllPreferenceConditionByMemberIdIn(List.of(targetMemberId))).thenReturn(List.of());
        when(preferenceConditionWeightRepository.findAllPreferenceConditionWeightByMemberIdIn(List.of(targetMemberId))).thenReturn(List.of());

        // When
        MatchDetailDto.Response response = roommateMatchingService.findMatchingDetail(targetMemberId, null);

        // Then
        assertThat(response.getMemberId()).isEqualTo(targetMemberId);
        assertThat(response.getMemberName()).isEqualTo("시커");
        assertThat(response.getMemberAge()).isEqualTo(DateUtils.calculateAge(birth));
        assertThat(response.getIsLike()).isFalse();
        assertThat(response.getRoomProfileType()).isEqualTo(RoomProfileType.SEEKER);
        assertThat(response.getOfferProfile()).isNull();
        assertThat(response.getSeekerProfile().getMinDeposit()).isEqualTo(300);
        assertThat(response.getSeekerProfile().getMaxDeposit()).isEqualTo(1000);
        assertThat(response.getSeekerProfile().getMinMonthlyRent()).isEqualTo(30);
        assertThat(response.getSeekerProfile().getMaxMonthlyRent()).isEqualTo(70);
        assertThat(response.getSeekerProfile().getRoomTypeNames()).containsExactly("투룸");
        assertThat(response.getSeekerProfile().getRegionFullNames()).containsExactly("서울특별시 성동구 성수동");
        assertThat(response.getAuthentications()).isEmpty();
        verifyNoInteractions(roomOfferProfileRepository);
        verify(memberInterestRepository, never()).existsBySenderIdAndReceiverId(any(), any());
    }

    @Test
    @DisplayName("상세 조회 대상 회원이 없으면 회원 없음 예외를 던지고 추가 정보를 조회하지 않는다")
    void findMatchingDetailThrowsWhenTargetMemberDoesNotExist() {
        // Given
        Long targetMemberId = 999L;
        when(memberRepository.findMatchingBasicRowById(targetMemberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roommateMatchingService.findMatchingDetail(targetMemberId, 10L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
        verifyNoInteractions(
                authenticationRepository,
                roomOfferProfileRepository,
                roomSeekerProfileRepository,
                memberLifePatternRepository,
                preferenceConditionRepository,
                preferenceConditionWeightRepository,
                memberInterestRepository
        );
    }
}
