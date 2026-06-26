package org.example.knockin.service.impl;

import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.global.exception.AgreementErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.agreement.AgreementLogRepository;
import org.example.knockin.repository.agreement.AgreementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("약관동의 관리 서비스 테스트")
class AgreementServiceImplTest {

    @Mock
    private AgreementRepository agreementRepository;

    @Mock
    private AgreementLogRepository agreementLogRepository;

    @InjectMocks
    private AgreementServiceImpl agreementService;

    @Test
    @DisplayName("약관 등록 성공 테스트")
    void saveAgreementSuccessTest() {
        // given
        Agreement agreement = Agreement.builder()
                .title("약관 제목")
                .contents("약관 내용")
                .isRequired(true)
                .type(1L)
                .build();

        given(agreementRepository.save(agreement)).willReturn(agreement);

        // when
        Agreement result = agreementService.saveAgreement(agreement);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("약관 제목");
        verify(agreementLogRepository).save(any(AgreementLog.class));
        verify(agreementRepository).save(agreement);
    }

    @Test
    @DisplayName("임시 약관 등록 성공 테스트")
    void modifyTemporaryAgreementSuccessTest() {
        // given
        Agreement agreement = Agreement.builder()
                .title("임시 약관")
                .contents("임시 내용")
                .type(1L)
                .build();
        given(agreementRepository.save(agreement)).willReturn(agreement);

        // when
        Agreement result = agreementService.modifyTemporaryAgreement(agreement);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("임시 약관");
        verify(agreementLogRepository).save(any(AgreementLog.class));
        verify(agreementRepository).save(agreement);
    }

    @Test
    @DisplayName("기존 약관 수정 성공 테스트")
    void modifyAgreementSuccessTest() {
        // given
        Long agreementId = 1L;
        Agreement existingAgreement = spy(Agreement.builder()
                .id(agreementId)
                .title("이전 제목")
                .contents("이전 내용")
                .isRequired(false)
                .type(1L)
                .build());
        Agreement newAgreement = Agreement.builder()
                .title("새로운 제목")
                .contents("새로운 내용")
                .isRequired(true)
                .type(1L)
                .build();

        given(agreementRepository.findById(agreementId)).willReturn(Optional.of(existingAgreement));
        given(agreementRepository.findByType(1L)).willReturn(List.of(existingAgreement));
        given(agreementLogRepository.findByAgreementIn(any())).willReturn(List.of(mock(AgreementLog.class)));

        // when
        Agreement result = agreementService.modifyAgreement(newAgreement, agreementId);

        // then
        assertThat(result).isNotNull();
        verify(existingAgreement).modifyAgreement(newAgreement);
        verify(agreementLogRepository).findByAgreementIn(any());
    }

    @Test
    @DisplayName("약관 수정 시 대상을 찾지 못하면 BusinessException 발생")
    void modifyAgreementNotFoundTest() {
        // given
        Long agreementId = 1L;
        Agreement newAgreement = Agreement.builder().build();
        given(agreementRepository.findById(agreementId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> agreementService.modifyAgreement(newAgreement, agreementId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AgreementErrorCode.AGREEMENT_NOT_FOUNT);
    }

    @Test
    @DisplayName("활성 약관 목록 조회 성공 테스트")
    void findAgreementListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Agreement agreement = Agreement.builder().id(100L).title("약관").build();
        AgreementLog log = AgreementLog.builder().agreement(agreement).isCurrent(true).build();

        given(agreementLogRepository.findByAgreemnetIsCurrent(true, pageable)).willReturn(List.of(log));

        // when
        List<Agreement> result = agreementService.findAgreementList(pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("단건 약관 상세 조회 성공 테스트")
    void findAgreementSuccessTest() {
        // given
        Long id = 100L;
        Agreement agreement = Agreement.builder().id(id).title("상세 약관").build();
        AgreementLog log = AgreementLog.builder().agreement(agreement).isCurrent(true).build();

        given(agreementLogRepository.findByAgreementIdAndIsCurrent(id, true)).willReturn(Optional.of(log));

        // when
        Agreement result = agreementService.findAgreement(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("단건 약관 상세 조회 시 대상을 찾지 못하면 BusinessException 발생")
    void findAgreementNotFoundTest() {
        // given
        Long id = 100L;
        given(agreementLogRepository.findByAgreementIdAndIsCurrent(id, true)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> agreementService.findAgreement(id))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AgreementErrorCode.AGREEMENT_NOT_FOUNT);
    }

    @Test
    @DisplayName("약관 삭제 성공 테스트")
    void deleteAgreementSuccessTest() {
        // given
        Long id = 100L;
        Agreement agreement = spy(Agreement.builder().id(id).isDeleted(false).build());

        given(agreementRepository.findById(id)).willReturn(Optional.of(agreement));

        // when
        Agreement result = agreementService.deleteAgreement(id);

        // then
        assertThat(result).isNotNull();
        verify(agreement).deleteAgreement();
    }

    @Test
    @DisplayName("약관 유형 조회 성공 테스트")
    void findMaxAgreementTypeSuccessTest() {
        // given
        Long id = 100L;
        Agreement agreement = Agreement.builder().id(id).type(3L).build();
        given(agreementRepository.findById(id)).willReturn(Optional.of(agreement));

        // when
        Long result = agreementService.findMaxAgreementType(id);

        // then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    @DisplayName("약관 유형 조회 시 대상을 찾지 못하면 BusinessException 발생")
    void findMaxAgreementTypeNotFoundTest() {
        // given
        Long id = 100L;
        given(agreementRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> agreementService.findMaxAgreementType(id))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AgreementErrorCode.AGREEMENT_NOT_FOUNT);
    }
}
