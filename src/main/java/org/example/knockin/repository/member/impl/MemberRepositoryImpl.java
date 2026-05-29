package org.example.knockin.repository.member.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.member.MemberRepositoryCustom;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.example.knockin.entity.member.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Member> searchMembers(String providerId, String providerType) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(providerIdEq(providerId))
                .fetch();
    }

    private BooleanExpression providerIdEq(String providerId) {
        return StringUtils.hasText(providerId) ? member.providerId.eq(providerId) : null;
    }
}
