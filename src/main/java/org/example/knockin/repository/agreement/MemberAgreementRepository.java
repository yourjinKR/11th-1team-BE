package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long>, MemberAgreementRepositoryCustom {
    List<MemberAgreement> findByMember(Member member);
}