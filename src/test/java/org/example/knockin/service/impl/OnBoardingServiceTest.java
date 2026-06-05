package org.example.knockin.service.impl;

import org.example.knockin.dto.SaveProfileBasicDto;
import org.example.knockin.dto.SaveProfileLifeStyleDto;
import org.example.knockin.dto.SaveProfileRoomInfoDto;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.life.MemberLifePattern;
import org.example.knockin.entity.member.BasicInformation;
import org.example.knockin.entity.member.Gender;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.room.*;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.global.exception.OnBoardErrorCode;
import org.example.knockin.repository.agreement.MemberAgreementRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.member.BasicInformationRepository;
import org.example.knockin.repository.room.OfferRoomTypeRepository;
import org.example.knockin.repository.room.RoomProfileRepository;
import org.example.knockin.repository.room.RoomSeekerProfileRegionRepository;
import org.example.knockin.repository.room.SeekerRoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnBoardingServiceTest {

    @Mock
    private BasicInformationRepository basicInformationRepository;
    @Mock
    private MemberServiceImpl memberService;
    @Mock
    private MemberAgreementRepository memberAgreementRepository;
    @Mock
    private MemberLifePatternRepository memberLifePatternRepository;
    @Mock
    private RoomProfileRepository roomProfileRepository;
    @Mock
    private OfferRoomTypeRepository offerRoomTypeRepository;
    @Mock
    private SeekerRoomTypeRepository seekerRoomTypeRepository;
    @Mock
    private RoomSeekerProfileRegionRepository roomSeekerProfileRegionRepository;
    @Mock
    private MetaServiceImpl metaService;

    @InjectMocks
    private OnBoardingServiceImpl onBoardingService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .providerId("test_provider")
                .providerType(null)
                .build();
    }

    @Test
    @DisplayName("기본 정보 저장 성공 테스트")
    void saveBasicInfoLogic_Success() {
        // given
        SaveProfileBasicDto.Request request = SaveProfileBasicDto.Request.builder()
                .name("홍길동")
                .birth(LocalDate.of(1995, 1, 1))
                .gender(Gender.MALE)
                .email("test@test.com")
                .terms(List.of(1L, 2L))
                .build();

        when(memberService.findById(1L)).thenReturn(Optional.of(testMember));
        when(basicInformationRepository.save(any(BasicInformation.class))).thenReturn(mock(BasicInformation.class));
        when(metaService.findByAgreementLogIsCurrent(anyList())).thenReturn(List.of(mock(AgreementLog.class)));
        when(memberAgreementRepository.saveAll(anyList())).thenReturn(List.of(mock(MemberAgreement.class)));

        // when
        SaveProfileBasicDto.Response response = onBoardingService.saveBasicInfoLogic(request, 1L);

        // then
        assertNotNull(response);
        verify(basicInformationRepository, times(1)).save(any());
        verify(memberAgreementRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("라이프스타일 저장 성공 테스트")
    void saveLifeStyleLogic_Success() {
        // given
        SaveProfileLifeStyleDto.Request request = SaveProfileLifeStyleDto.Request.builder()
                .lifestyles(List.of(1L, 2L))
                .build();

        when(memberService.findById(1L)).thenReturn(Optional.of(testMember));
        when(metaService.findByLifeStyle(anyList())).thenReturn(List.of(mock(LifePatternInformation.class)));
        when(memberLifePatternRepository.saveAll(anyList())).thenReturn(List.of(mock(MemberLifePattern.class)));

        // when
        SaveProfileLifeStyleDto.Response response = onBoardingService.saveLifeStyleLogic(request, 1L);

        // then
        assertNotNull(response);
        verify(memberLifePatternRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("방 정보 저장 성공 테스트 (OFFER 타입)")
    void saveRoomInfoLogic_Offer_Success() {
        // given
        SaveProfileRoomInfoDto.Request request = SaveProfileRoomInfoDto.Request.builder()
                .type(RoomProfileType.OFFER)
                .region(List.of(1L))
                .deposit(1000)
                .mounthRent(50)
                .isComeableAtNegotiable(true)
                .comeEnableAt(LocalDateTime.now())
                .roomProfile(List.of(1L, 2L))
                .build();

        when(memberService.findById(1L)).thenReturn(Optional.of(testMember));
        when(metaService.findByRegionId(1L)).thenReturn(Optional.of(mock(Region.class)));
        when(roomProfileRepository.save(any(RoomOfferProfile.class))).thenReturn(mock(RoomOfferProfile.class));
        when(metaService.findByRoomTypes(anyList())).thenReturn(List.of(mock(RoomType.class)));

        // when
        SaveProfileRoomInfoDto.Response response = onBoardingService.saveRoomInfoLogic(request, 1L);

        // then
        assertNotNull(response);
        verify(roomProfileRepository, times(1)).save(any());
        verify(offerRoomTypeRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("방 정보 저장 성공 테스트 (SEEKER 타입)")
    void saveRoomInfoLogic_Seeker_Success() {
        // given
        SaveProfileRoomInfoDto.Request request = SaveProfileRoomInfoDto.Request.builder()
                .type(RoomProfileType.SEEKER)
                .region(List.of(1L, 2L))
                .minDeposit(500)
                .maxDeposit(2000)
                .minMounthRent(30)
                .maxMounthRent(100)
                .isComeableAtNegotiable(false)
                .comeEnableAt(LocalDateTime.now())
                .roomProfile(List.of(1L))
                .build();

        when(memberService.findById(1L)).thenReturn(Optional.of(testMember));
        when(roomProfileRepository.save(any(RoomSeekerProfile.class))).thenReturn(mock(RoomSeekerProfile.class));
        when(metaService.findByRegions(anyList())).thenReturn(List.of(mock(Region.class)));
        when(metaService.findByRoomTypes(anyList())).thenReturn(List.of(mock(RoomType.class)));

        // when
        SaveProfileRoomInfoDto.Response response = onBoardingService.saveRoomInfoLogic(request, 1L);

        // then
        assertNotNull(response);
        verify(roomProfileRepository, times(1)).save(any());
        verify(roomSeekerProfileRegionRepository, times(1)).saveAll(any());
        verify(seekerRoomTypeRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("회원 정보를 찾을 수 없을 때 예외 발생 테스트")
    void saveBasicInfoLogic_MemberNotFound_ThrowsException() {
        // given
        SaveProfileBasicDto.Request request = SaveProfileBasicDto.Request.builder().build();
        when(memberService.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> onBoardingService.saveBasicInfoLogic(request, 1L));
    }
}
