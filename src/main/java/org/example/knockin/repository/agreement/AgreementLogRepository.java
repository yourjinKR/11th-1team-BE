package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.entity.agreement.AgreementLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AgreementLogRepository extends JpaRepository<AgreementLog, Long>, AgreementLogRepositoryCustom {
    Optional<AgreementLog> findByAgreementIdAndIsCurrent(Long agreementId, Boolean isCurrent);

    List<AgreementLog> findByIsCurrent(Boolean isCurrent, Pageable pageable);

    List<AgreementLog> findByAgreementIn(Collection<Agreement> agreements);
}