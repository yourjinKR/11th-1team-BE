package org.example.knockin.repository.agreement;

import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.member.Member;

import java.util.List;

public interface MemberAgreementRepositoryCustom {
    List<MemberAgreement> findByMemberAndAgreementLogNotIn(Member member, List<AgreementLog> skipList);
}
