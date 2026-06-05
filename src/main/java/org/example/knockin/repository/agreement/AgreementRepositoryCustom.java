package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgreementRepositoryCustom {
    List<Agreement> findByAgreements(List<Long> agreementId);
}