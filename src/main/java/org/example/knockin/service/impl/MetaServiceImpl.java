package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.repository.agreement.AgreementLogRepository;
import org.example.knockin.repository.agreement.AgreementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaServiceImpl {
    private final AgreementRepository agreementRepository;
    private final AgreementLogRepository agreementLogRepository;

    public List<Agreement> findByAgreement(List<Long> agreementId) {
        return agreementRepository.findByAgreements(agreementId);
    }

    public List<AgreementLog> findByAgreementLogIsCurrent(List<Long> agreementIds) {
        return agreementLogRepository.findByAgreementLogIsCurrent(agreementIds);
    }
}
