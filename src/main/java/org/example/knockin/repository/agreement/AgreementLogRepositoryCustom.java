package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.AgreementLog;

import java.util.List;

public interface AgreementLogRepositoryCustom {
    List<AgreementLog> findByAgreementLogIsCurrent(List<Long> agreementIds);
}