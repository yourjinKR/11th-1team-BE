package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.AgreementLog;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AgreementLogRepositoryCustom {
    List<AgreementLog> findByAgreementLogIsCurrent(List<Long> agreementIds);
    List<AgreementLog> findByAgreemnetIsCurrent(boolean isCurrent, Pageable pageable);
}