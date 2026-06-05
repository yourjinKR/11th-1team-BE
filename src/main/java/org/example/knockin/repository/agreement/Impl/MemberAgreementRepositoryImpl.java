package org.example.knockin.repository.agreement.Impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.repository.agreement.MemberAgreementRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberAgreementRepositoryImpl implements MemberAgreementRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
}
