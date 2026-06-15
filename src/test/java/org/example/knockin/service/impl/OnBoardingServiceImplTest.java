package org.example.knockin.service.impl;

import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.life.*;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberPrivacy;
import org.example.knockin.entity.member.MemberPrivacyType;
import org.example.knockin.entity.room.*;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.agreement.MemberAgreementRepository;
import org.example.knockin.repository.life.*;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.member.MemberPrivacyRepository;
import org.example.knockin.repository.room.OfferRoomTypeRepository;
import org.example.knockin.repository.room.RoomProfileRepository;
import org.example.knockin.repository.room.RoomSeekerProfileRegionRepository;
import org.example.knockin.repository.room.SeekerRoomTypeRepository;
import org.example.knockin.service.RoommateBoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnBoardingServiceImplTest {

    @Mock private BasicInformationRepository basicInformationRepository;
    @Mock private MemberServiceImpl memberService;
    @Mock private MemberAgreementRepository memberAgreementRepository;
    @Mock private MemberLifePatternRepository memberLifePatternRepository;
    @Mock private MemberLifePatternLogRepository memberLifePatternLogRepository;
    @Mock private RoomProfileRepository roomProfileRepository;
    @Mock private OfferRoomTypeRepository offerRoomTypeRepository;
    @Mock private SeekerRoomTypeRepository seekerRoomTypeRepository;
    @Mock private RoomSeekerProfileRegionRepository roomSeekerProfileRegionRepository;
    @Mock private MetaServiceImpl metaService;
    @Mock private LifePatternInformationRepository lifePatternInformationRepository;
    @Mock private PreferenceConditionRepository preferenceConditionRepository;
    @Mock private PreferenceConditionLogRepository preferenceConditionLogRepository;
    @Mock private PreferenceConditionWeightRepository preferenceConditionWeightRepository;
    @Mock private PreferenceConditionWeightLogRepository preferenceConditionWeightLogRepository;
    @Mock private LifePatternRepository lifePatternRepository;
    @Mock private MemberPrivacyRepository memberPrivacyRepository;
    @Mock private MyRoomMateServiceImpl myRoomMateService;
    @Mock private RoommateBoardService roommateBoardService;

    @InjectMocks
    private OnBoardingServiceImpl onBoardingService;

    private Member member;
    private Long memberId = 1L;

    @BeforeEach
    void setUp() {
        member = Member.builder().id(memberId).build();
        lenient().when(memberService.findById(memberId)).thenReturn(Optional.of(member));
        lenient().when(memberService.isOnBoarding(any())).thenReturn(true);
        lenient().when(memberPrivacyRepository.findByMember(any())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("기본 정보 저장 성공 테스트")
    void saveBasicInfoLogic_Success() {
        SaveProfileBasicDto.Request request = SaveProfileBasicDto.Request.builder()
                .name("홍길동")
                .terms(List.of(1L))
                .build();

        given(basicInformationRepository.save(any())).willReturn(mock(BasicInformation.class));
        given(metaService.findByAgreementLogIsCurrent(any())).willReturn(List.of(mock(AgreementLog.class)));
        given(memberAgreementRepository.saveAll(any())).willReturn(List.of(mock(MemberAgreement.class)));

        SaveProfileBasicDto.Response response = onBoardingService.saveBasicInfoLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(basicInformationRepository).save(any());
        verify(memberAgreementRepository).saveAll(any());
        verify(memberPrivacyRepository).save(any());
    }

    @Test
    @DisplayName("라이프스타일 저장 성공 테스트")
    void saveLifeStyleLogic_Success() {
        SaveProfileLifeStyleDto.Request request = SaveProfileLifeStyleDto.Request.builder()
                .lifestyles(List.of(1L))
                .build();

        given(metaService.findByLifeStyle(any())).willReturn(List.of(mock(LifePatternInformation.class)));
        given(memberLifePatternRepository.saveAll(any())).willReturn(List.of(mock(MemberLifePattern.class)));

        SaveProfileLifeStyleDto.Response response = onBoardingService.saveLifeStyleLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(memberLifePatternRepository).saveAll(any());
    }

    @Test
    @DisplayName("방 정보 저장 성공 테스트 (OFFER)")
    void saveRoomInfoLogic_Offer_Success() {
        SaveProfileRoomInfoDto.Request request = SaveProfileRoomInfoDto.Request.builder()
                .type(RoomProfileType.OFFER)
                .region(List.of(1L))
                .roomProfile(List.of(1L))
                .build();

        given(metaService.findByRegionId(anyLong())).willReturn(Optional.of(mock(Region.class)));
        given(roomProfileRepository.save(any())).willReturn(mock(RoomOfferProfile.class));
        given(metaService.findByRoomTypes(any())).willReturn(List.of(mock(RoomType.class)));

        SaveProfileRoomInfoDto.Response response = onBoardingService.saveRoomInfoLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(roomProfileRepository).save(any());
        verify(offerRoomTypeRepository).saveAll(any());
    }

    @Test
    @DisplayName("방 정보 저장 성공 테스트 (SEEKER)")
    void saveRoomInfoLogic_Seeker_Success() {
        SaveProfileRoomInfoDto.Request request = SaveProfileRoomInfoDto.Request.builder()
                .type(RoomProfileType.SEEKER)
                .region(List.of(1L))
                .roomProfile(List.of(1L))
                .build();

        given(roomProfileRepository.save(any())).willReturn(mock(RoomSeekerProfile.class));
        given(metaService.findByRegions(any())).willReturn(List.of(mock(Region.class)));
        given(metaService.findByRoomTypes(any())).willReturn(List.of(mock(RoomType.class)));

        SaveProfileRoomInfoDto.Response response = onBoardingService.saveRoomInfoLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(roomProfileRepository).save(any());
        verify(roomSeekerProfileRegionRepository).saveAll(any());
        verify(seekerRoomTypeRepository).saveAll(any());
    }

    @Test
    @DisplayName("전체 프로필 저장 성공 테스트")
    void saveAll_Success() {
        SaveProfileAllDto.Request request = new SaveProfileAllDto.Request();
        request.setType(RoomProfileType.OFFER);
        request.setTerms(List.of(1L));
        request.setLifestyles(List.of(1L));
        request.setRegion(List.of(1L));
        request.setRoomProfile(List.of(1L));

        given(basicInformationRepository.save(any())).willReturn(mock(BasicInformation.class));
        given(metaService.findByAgreementLogIsCurrent(any())).willReturn(List.of(mock(AgreementLog.class)));
        given(memberAgreementRepository.saveAll(any())).willReturn(List.of(mock(MemberAgreement.class)));
        given(metaService.findByLifeStyle(any())).willReturn(List.of(mock(LifePatternInformation.class)));
        given(memberLifePatternRepository.saveAll(any())).willReturn(List.of(mock(MemberLifePattern.class)));
        given(metaService.findByRegionId(anyLong())).willReturn(Optional.of(mock(Region.class)));
        given(roomProfileRepository.save(any())).willReturn(mock(RoomOfferProfile.class));
        given(metaService.findByRoomTypes(any())).willReturn(List.of(mock(RoomType.class)));

        SaveProfileAllDto.Response response = onBoardingService.saveAll(request, memberId);

        assertThat(response).isNotNull();
        verify(basicInformationRepository).save(any());
        verify(memberLifePatternRepository).saveAll(any());
        verify(roomProfileRepository).save(any());
    }

    @Test
    @DisplayName("기본 정보 수정 성공 테스트")
    void modifyBasicInfoLogic_Success() {
        ModifyProfileBasicDto.Request request = ModifyProfileBasicDto.Request.builder()
                .name("이몽룡")
                .terms(List.of(1L))
                .build();

        BasicInformation basicInfo = mock(BasicInformation.class);
        given(basicInformationRepository.findByMember(member)).willReturn(List.of(basicInfo));
        given(metaService.findByAgreementLogIsCurrent(any())).willReturn(List.of(mock(AgreementLog.class)));
        given(memberAgreementRepository.findByMember(member)).willReturn(Collections.emptyList());

        ModifyProfileBasicDto.Response response = onBoardingService.modifyBasicInfoLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(basicInfo).modifyBasicInformation(request);
    }

    @Test
    @DisplayName("프로필 공개 여부 변경 성공 테스트")
    void changeProfileStatus_Success() {
        ProfileVisibilityDto.Request request = new ProfileVisibilityDto.Request();
        request.setStatus(MemberPrivacyType.PUBLIC);

        given(myRoomMateService.isExistRoomMate(member)).willReturn(false);
        MemberPrivacy privacy = mock(MemberPrivacy.class);
        given(memberPrivacyRepository.findByMember(member)).willReturn(List.of(privacy));

        ProfileVisibilityDto.Response response = onBoardingService.changeProfileStatus(request, memberId);

        assertThat(response).isNotNull();
        verify(privacy).changeState(MemberPrivacyType.PUBLIC);
    }

    @Test
    @DisplayName("룸메이트가 있을 때 프로필 상태 변경 시 예외 발생")
    void changeProfileStatus_Fail_WhenRoomMateExists() {
        ProfileVisibilityDto.Request request = new ProfileVisibilityDto.Request();
        given(myRoomMateService.isExistRoomMate(member)).willReturn(true);

        assertThatThrownBy(() -> onBoardingService.changeProfileStatus(request, memberId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("라이프스타일 수정 성공 테스트")
    void modifyLifeStyleLogic_Success() {
        ModifyProfileLifeStyleDto.Request.LifeStyleInfo info = new ModifyProfileLifeStyleDto.Request.LifeStyleInfo();
        info.setId(1L);
        info.setLifestyleId(10L);
        ModifyProfileLifeStyleDto.Request request = ModifyProfileLifeStyleDto.Request.builder()
                .lifestyles(List.of(info))
                .build();

        LifePattern lifePattern = mock(LifePattern.class);
        given(lifePattern.getId()).willReturn(100L);

        LifePatternInformation oldInfo = mock(LifePatternInformation.class);
        given(oldInfo.getLifePattern()).willReturn(lifePattern);

        MemberLifePattern memberLifePattern = mock(MemberLifePattern.class);
        given(memberLifePattern.getId()).willReturn(1L);
        given(memberLifePattern.getLifePatternInformation()).willReturn(oldInfo);

        LifePatternInformation newInfo = mock(LifePatternInformation.class);
        given(newInfo.getLifePattern()).willReturn(lifePattern);
        given(newInfo.getId()).willReturn(10L);

        given(lifePatternInformationRepository.findAllById(any())).willReturn(List.of(newInfo));
        given(memberLifePatternRepository.findByMember(member)).willReturn(List.of(memberLifePattern));

        ModifyProfileLifeStyleDto.Response response = onBoardingService.modifyLifeStyleLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(memberLifePattern).modifyLifePatternInformation(newInfo);
    }

    @Test
    @DisplayName("방 정보 수정 테스트 (OFFER -> SEEKER 전환)")
    void modifyRoomInfoLogic_Switch_Success() {
        ModifyProfileRoomInfoDto.Request request = ModifyProfileRoomInfoDto.Request.builder()
                .type(RoomProfileType.SEEKER)
                .region(List.of(1L))
                .roomProfile(List.of(1L))
                .build();

        RoomOfferProfile oldProfile = mock(RoomOfferProfile.class);
        given(oldProfile.getType()).willReturn(RoomProfileType.OFFER);
        given(roomProfileRepository.findByMember(member)).willReturn(List.of(oldProfile));
        given(roomProfileRepository.save(any())).willReturn(mock(RoomSeekerProfile.class));
        given(metaService.findByRegions(any())).willReturn(List.of(mock(Region.class)));
        given(metaService.findByRoomTypes(any())).willReturn(List.of(mock(RoomType.class)));

        ModifyProfileRoomInfoDto.Response response = onBoardingService.modifyRoomInfoLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(offerRoomTypeRepository).deleteByRoomOfferProfile(oldProfile);
        verify(roomProfileRepository).delete(oldProfile);
        verify(roomProfileRepository).save(any(RoomSeekerProfile.class));
    }

    @Test
    @DisplayName("선호 조건 저장 성공 테스트")
    void savePreferenceConditionLogic_Success() {
        SavePreferencesConditionsDto.Request request = new SavePreferencesConditionsDto.Request(List.of(1L));

        given(metaService.findLifePatternByLifeStyle(any())).willReturn(List.of(mock(LifePattern.class)));
        given(preferenceConditionWeightRepository.saveAll(any())).willReturn(List.of(mock(PreferenceConditionWeight.class)));
        given(preferenceConditionWeightLogRepository.saveAll(any())).willReturn(List.of(mock(PreferenceConditionWeightLog.class)));

        SavePreferencesConditionsDto.Response response = onBoardingService.savePreferenceConditionLogic(request, memberId);

        assertThat(response).isNotNull();
        verify(preferenceConditionWeightRepository).saveAll(any());
    }

    @Test
    @DisplayName("내가 쓴 게시글 조회 테스트")
    void findMyBoardList_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<MyBoardListDto.Response.BoardItem> page = new PageImpl<>(List.of(mock(MyBoardListDto.Response.BoardItem.class)));
        given(roommateBoardService.getMyBoardList(any(), any())).willReturn(page);

        MyBoardListDto.Response response = onBoardingService.findMyBoardList(pageable, memberId);

        assertThat(response.getBoards()).hasSize(1);
    }
}
