package org.example.knockin.service.impl;

import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.life.LifePattern;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.entity.room.Region;
import org.example.knockin.entity.room.RoomExtraOption;
import org.example.knockin.entity.room.RoomType;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.agreement.AgreementLogRepository;
import org.example.knockin.repository.agreement.AgreementRepository;
import org.example.knockin.repository.life.LifePatternInformationRepository;
import org.example.knockin.repository.life.LifePatternRepository;
import org.example.knockin.repository.member.SearchRepository;
import org.example.knockin.repository.room.RegionRepository;
import org.example.knockin.repository.room.RoomExtraOptionRepository;
import org.example.knockin.repository.room.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MetaServiceImplTest {

    @Mock private AgreementLogRepository agreementLogRepository;
    @Mock private LifePatternInformationRepository lifePatternInformationRepository;
    @Mock private LifePatternRepository lifePatternRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private RoomTypeRepository roomTypeRepository;
    @Mock private RoomExtraOptionRepository roomExtraOptionRepository;
    @Mock private AgreementRepository agreementRepository;
    @Mock private SearchRepository searchRepository;

    @InjectMocks
    private MetaServiceImpl metaService;

    @Test
    @DisplayName("약관 목록 조회 테스트")
    void findTermListTest() {
        Agreement agreement = mock(Agreement.class);
        given(agreement.getId()).willReturn(1L);
        given(agreement.getTitle()).willReturn("약관 제목");
        given(agreementRepository.findAllByIsDeleted(false)).willReturn(Optional.of(agreement));

        TermsListDto.Response result = metaService.findTermList();

        assertThat(result.getTerms()).hasSize(1);
        assertThat(result.getTerms().get(0).getTitle()).isEqualTo("약관 제목");
    }

    @Test
    @DisplayName("약관 상세 조회 테스트")
    void findTermDetailTest() {
        Agreement agreement = mock(Agreement.class);
        given(agreement.getId()).willReturn(1L);
        given(agreement.getContents()).willReturn("약관 내용");
        given(agreementRepository.findById(anyLong())).willReturn(Optional.of(agreement));

        TermsDetailDto.Response result = metaService.findTermDetail(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContents()).isEqualTo("약관 내용");
    }

    @Test
    @DisplayName("인기 검색어 조회 테스트")
    void findPopSearchTest() {
        PopularSearchDto.Response.RankItem rankItem = PopularSearchDto.Response.RankItem.builder().keyword("키워드").build();
        given(searchRepository.findPopSearch()).willReturn(List.of(rankItem));

        PopularSearchDto.Response result = metaService.findPopSearch();

        assertThat(result.getRank()).hasSize(1);
        assertThat(result.getRank().get(0).getId()).isEqualTo(1L);
        assertThat(result.getRank().get(0).getKeyword()).isEqualTo("키워드");
    }

    @Test
    @DisplayName("라이프스타일 패턴 메타데이터 조회 테스트")
    void findLifeStylePatternsTest() {
        MetaLifestylePatternsDto.Response.PatternItem item = MetaLifestylePatternsDto.Response.PatternItem.builder().id(1L).name("패턴").build();
        given(lifePatternRepository.findLifeStylePatterns()).willReturn(List.of(item));

        MetaLifestylePatternsDto.Response result = metaService.findLifeStylePatterns();

        assertThat(result.getPatterns()).hasSize(1);
        assertThat(result.getPatterns().get(0).getName()).isEqualTo("패턴");
    }

    @Test
    @DisplayName("지역 메타데이터 조회 테스트")
    void findRegionsTest() {
        Region region = mock(Region.class);
        Region parent = mock(Region.class);
        given(region.getId()).willReturn(1L);
        given(region.getName()).willReturn("서울");
        given(region.getParent()).willReturn(parent);
        given(parent.getId()).willReturn(0L);
        given(regionRepository.findAll()).willReturn(List.of(region));

        MetaRegionsDto.Response result = metaService.findRegions();

        assertThat(result.getRegion()).hasSize(1);
        assertThat(result.getRegion().get(0).getName()).isEqualTo("서울");
    }

    @Test
    @DisplayName("방 유형 메타데이터 조회 테스트")
    void findRoomTypesTest() {
        RoomType roomType = mock(RoomType.class);
        given(roomType.getId()).willReturn(1L);
        given(roomType.getName()).willReturn("원룸");
        given(roomTypeRepository.findAllByIsDeleted(false)).willReturn(List.of(roomType));

        MetaRoomTypesDto.Response result = metaService.findRoomTypes();

        assertThat(result.getRoomType()).hasSize(1);
        assertThat(result.getRoomType().get(0).getName()).isEqualTo("원룸");
    }

    @Test
    @DisplayName("방 추가 옵션 메타데이터 조회 테스트")
    void findRoomAddOptionsTest() {
        RoomExtraOption option = mock(RoomExtraOption.class);
        given(option.getId()).willReturn(1L);
        given(option.getName()).willReturn("에어컨");
        given(roomExtraOptionRepository.findAllByIsDeleted(false)).willReturn(List.of(option));

        MetaRoomAddOptionsDto.Response result = metaService.findRoomAddOptions();

        assertThat(result.getRoomAddOption()).hasSize(1);
        assertThat(result.getRoomAddOption().get(0).getName()).isEqualTo("에어컨");
    }

    @Test
    @DisplayName("ID로 지역 찾기 테스트")
    void findByRegionIdTest() {
        Region region = mock(Region.class);
        given(regionRepository.findById(1L)).willReturn(Optional.of(region));

        Optional<Region> result = metaService.findByRegionId(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("여러 ID로 방 유형 찾기 테스트")
    void findByRoomTypesTest() {
        RoomType roomType = mock(RoomType.class);
        given(roomTypeRepository.findByRoomTypes(anyList())).willReturn(List.of(roomType));

        List<RoomType> result = metaService.findByRoomTypes(List.of(1L));

        assertThat(result).hasSize(1);
    }
}
