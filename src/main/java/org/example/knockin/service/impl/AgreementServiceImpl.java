package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.global.exception.AgreementErrorCode;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.agreement.AgreementLogRepository;
import org.example.knockin.repository.agreement.AgreementRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgreementServiceImpl {
    private final AgreementRepository agreementRepository;
    private final AgreementLogRepository agreementLogRepository;

    @Transactional
    public Agreement saveAgreement(Agreement agreement) {
        Agreement agreementEntity = agreementRepository.save(agreement);
        agreementLogRepository.save(AgreementLog.builder().agreement(agreementEntity).isCurrent(true).build());
        return agreementEntity;
    }

    public Long findMaxAgreementType(Long agreementId) {
        return agreementRepository.findById(agreementId).orElseThrow(() -> new BusinessException(AgreementErrorCode.AGREEMENT_NOT_FOUNT)).getType();
    }

    @Transactional
    public Agreement modifyTemporaryAgreement(Agreement agreement) {
        Agreement agreementEntity = agreementRepository.save(agreement);
        agreementLogRepository.save(AgreementLog.builder().agreement(agreement).isCurrent(false).build());
        return agreementEntity;
    }

    @Transactional
    public Agreement modifyAgreement(Agreement agreement, Long agreementId) {
        Agreement agreementEntity = agreementRepository.findById(agreementId).orElseThrow(() -> new BusinessException(AgreementErrorCode.AGREEMENT_NOT_FOUNT));
        agreementLogRepository.findByAgreementIn(agreementRepository.findByType(agreementEntity.getType())).forEach(AgreementLog::clearCurrent);
        agreementEntity.modifyAgreement(agreement);
        return agreementEntity;
    }

    public List<Agreement> findAgreementList(Pageable pageable) {
        return agreementLogRepository.findByAgreemnetIsCurrent(true, pageable).stream().map(AgreementLog::getAgreement).toList();
    }

    public Agreement findAgreement(Long id) {
        return agreementLogRepository.findByAgreementIdAndIsCurrent(id, true)
                .orElseThrow(() -> new BusinessException(AgreementErrorCode.AGREEMENT_NOT_FOUNT)).getAgreement();
    }

    @Transactional
    public Agreement deleteAgreement(Long id) {
        Agreement agreement = agreementRepository.findById(id).orElseThrow(() -> new BusinessException(AgreementErrorCode.AGREEMENT_NOT_FOUNT));
        agreement.deleteAgreement();
        return agreement;
    }
}
