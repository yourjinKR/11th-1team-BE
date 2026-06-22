package org.example.knockin.service.impl;

import org.example.knockin.dto.*;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.room.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 백오피스 서비스 테스트")
class BackOfficeServiceImplTest {

    @Mock
    private AgreementServiceImpl agreementService;

    @Mock
    private RoomTypeServiceImpl roomTypeService;

    @InjectMocks
    private BackOfficeServiceImpl backOfficeService;

    @Test
    @DisplayName("약관 등록 성공 테스트 (saveTerms)")
    void saveTermsSuccessTest() {
        // given
        BoTermsDto.Request request = new BoTermsDto.Request();
        request.setTitle("약관 제목");
        request.setContents("약관 내용");
        request.setIsRequired(true);

        // when
        BoTermsDto.Response response = backOfficeService.saveTerms(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).saveAgreement(any(Agreement.class));
    }

    @Test
    @DisplayName("임시 약관 수정 성공 테스트 (modifyTerms)")
    void modifyTermsSuccessTest() {
        // given
        Long termsId = 100L;
        BoTermsDto.Request request = new BoTermsDto.Request();
        request.setTitle("임시 약관 제목");
        request.setContents("임시 약관 내용");
        request.setIsRequired(false);

        // when
        BoTermsDto.Response response = backOfficeService.modifyTerms(request, termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).modifyTemporaryAgreement(any(Agreement.class), eq(termsId));
    }

    @Test
    @DisplayName("약관 목록 조회 성공 테스트 (findTermsList)")
    void findTermsListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Agreement agreement = Agreement.builder()
                .id(100L)
                .title("서비스 약관")
                .build();
        ReflectionTestUtils.setField(agreement, "createdAt", LocalDateTime.now());

        given(agreementService.findAgreementList(pageable)).willReturn(List.of(agreement));

        // when
        BoTermsListDto.Response response = backOfficeService.findTermsList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTerms()).hasSize(1);
        BoTermsListDto.Response.TermsItem item = response.getTerms().get(0);
        assertThat(item.getId()).isEqualTo(100L);
        assertThat(item.getTitle()).isEqualTo("서비스 약관");
        assertThat(item.getCreateAt()).isNotNull();
    }

    @Test
    @DisplayName("약관 상세 조회 성공 테스트 (findTerms)")
    void findTermsSuccessTest() {
        // given
        Long termsId = 100L;
        Agreement agreement = Agreement.builder()
                .id(termsId)
                .title("상세 약관")
                .contents("상세 내용")
                .build();
        ReflectionTestUtils.setField(agreement, "createdAt", LocalDateTime.now());

        given(agreementService.findAgreement(termsId)).willReturn(agreement);

        // when
        BoTermsDetailDto.Response response = backOfficeService.findTerms(termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(termsId);
        assertThat(response.getTitle()).isEqualTo("상세 약관");
        assertThat(response.getContents()).isEqualTo("상세 내용");
        assertThat(response.getCreateAt()).isNotNull();
    }

    @Test
    @DisplayName("약관 삭제 성공 테스트 (deleteTerms)")
    void deleteTermsSuccessTest() {
        // given
        Long termsId = 100L;

        // when
        BoTermsDto.Response response = backOfficeService.deleteTerms(termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).deleteAgreement(termsId);
    }

    @Test
    @DisplayName("마지막 승인 약관 수정 성공 테스트 (modifyLastTerms)")
    void modifyLastTermsSuccessTest() {
        // given
        Long termsId = 100L;
        BoTermsDto.Request request = new BoTermsDto.Request();
        request.setTitle("최종 약관");
        request.setContents("최종 내용");
        request.setIsRequired(true);

        // when
        BoTermsDto.Response response = backOfficeService.modifyLastTerms(request, termsId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(agreementService).modifyAgreement(any(Agreement.class), eq(termsId));
    }

    @Test
    @DisplayName("방 형태 등록 성공 테스트 (saveRoomType)")
    void saveRoomTypeSuccessTest() {
        // given
        BoRoomTypeDto.Request request = new BoRoomTypeDto.Request();
        request.setName("원룸");

        // when
        BoRoomTypeDto.Response response = backOfficeService.saveRoomType(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(roomTypeService).saveRoomType(any(RoomType.class));
    }

    @Test
    @DisplayName("방 형태 목록 조회 성공 테스트 (findRoomTypeList)")
    void findRoomTypeListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        RoomType roomType = RoomType.builder().id(1L).name("원룸").build();

        given(roomTypeService.findRoomTypeList(pageable)).willReturn(List.of(roomType));

        // when
        BoRoomTypeListDto.Response response = backOfficeService.findRoomTypeList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRoomType()).hasSize(1);
        assertThat(response.getRoomType().get(0).getId()).isEqualTo(1L);
        assertThat(response.getRoomType().get(0).getName()).isEqualTo("원룸");
    }

    @Test
    @DisplayName("방 형태 수정 성공 테스트 (modifyRoomType)")
    void modifyRoomTypeSuccessTest() {
        // given
        Long roomTypeId = 1L;
        BoRoomTypeDto.Request request = new BoRoomTypeDto.Request();
        request.setName("투룸");

        // when
        BoRoomTypeDto.Response response = backOfficeService.modifyRoomType(request, roomTypeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(roomTypeService).modifyRoomType(any(RoomType.class), eq(roomTypeId));
    }

    @Test
    @DisplayName("방 형태 삭제 성공 테스트 (deleteRoomType)")
    void deleteRoomTypeSuccessTest() {
        // given
        Long roomTypeId = 1L;

        // when
        BoRoomTypeDto.Response response = backOfficeService.deleteRoomType(roomTypeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(roomTypeService).deleteRoomType(roomTypeId);
    }

    @Test
    @DisplayName("방 형태 상세 조회 성공 테스트 (findRoomType)")
    void findRoomTypeSuccessTest() {
        // given
        Long roomTypeId = 1L;
        RoomType roomType = RoomType.builder().id(roomTypeId).name("원룸").build();

        given(roomTypeService.findRoomType(roomTypeId)).willReturn(roomType);

        // when
        BoRoomTypeDetailDto.Response response = backOfficeService.findRoomType(roomTypeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(roomTypeId);
        assertThat(response.getName()).isEqualTo("원룸");
    }
}
