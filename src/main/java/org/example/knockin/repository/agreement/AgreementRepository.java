package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgreementRepository extends JpaRepository<Agreement, Long>, AgreementRepositoryCustom {
    Optional<Agreement> findAllByIsDeleted(Boolean isDeleted);
}