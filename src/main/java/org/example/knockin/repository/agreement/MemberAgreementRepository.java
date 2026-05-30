package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {
}