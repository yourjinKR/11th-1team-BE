package org.example.knockin.repository.agreement.Impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.agreement.Agreement;
import org.example.knockin.repository.agreement.AgreementRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;
import static org.example.knockin.entity.agreement.QAgreement.agreement;
import static org.example.knockin.entity.agreement.QAgreementLog.agreementLog;

@Repository
@RequiredArgsConstructor
public class AgreementRepositoryImpl implements AgreementRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Agreement> findByAgreements(List<Long> agreementId) {
        return jpaQueryFactory.selectFrom(agreement).where(agreement.id.in(agreementId)).fetch();
    }

    @Override
    public List<Agreement> findByAgreementsIsCurrentAndIsDeleted() {
        return jpaQueryFactory.selectFrom(agreement).join(agreementLog).on(agreementLog.agreement.eq(agreement), agreementLog.isCurrent.eq(true)).where(agreement.isDeleted.eq(false)).fetch();
    }
}