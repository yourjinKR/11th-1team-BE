package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
}