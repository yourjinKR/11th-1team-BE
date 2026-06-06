package org.example.knockin.repository.agreement.Impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.agreement.MemberAgreementRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;
import static org.example.knockin.entity.agreement.QMemberAgreement.memberAgreement;

@Repository
@RequiredArgsConstructor
public class MemberAgreementRepositoryImpl implements MemberAgreementRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberAgreement> findByMemberAndAgreementLogNotIn(Member member, List<AgreementLog> skipList) {
        return jpaQueryFactory.selectFrom(memberAgreement).where(memberAgreement.member.eq(member), memberAgreement.agreementLog.notIn(skipList)).fetch();
    }
}
