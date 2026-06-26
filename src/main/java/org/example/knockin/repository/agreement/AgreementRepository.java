package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgreementRepository extends JpaRepository<Agreement, Long>, AgreementRepositoryCustom {
    List<Agreement> findAllByIsDeleted(Boolean isDeleted);

    List<Agreement> findByType(Long type);
}