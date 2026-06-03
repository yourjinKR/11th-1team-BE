package org.example.knockin.repository.member.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.dto.AuthResponse;
import org.example.knockin.repository.member.MemberRepositoryCustom;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static org.example.knockin.entity.member.QMember.member;
import static org.example.knockin.entity.life.QPreferenceCondition.preferenceCondition;
import static org.example.knockin.entity.life.QMemberLifePattern.memberLifePattern;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Member> findMemberByProvider(String providerId, LoginProviderType providerType) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(member)
                .where(providerIdEq(providerId), providerTypeEq(providerType))
                .fetchOne());
    }

    public Optional<AuthResponse> findMemberInfo(Member memberEntity) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(AuthResponse.class,
                        JPAExpressions.selectOne()
                                .from(memberLifePattern)
                                .where(memberLifePattern.member.eq(member))
                                .exists().as("basicInfo"),
                        JPAExpressions.selectOne()
                                .from(preferenceCondition)
                                .where(preferenceCondition.member.eq(member))
                                .exists().as("preferenceInfo")
                ))
                .from(member)
                .where(member.id.eq(memberEntity.getId()))
                .fetchOne());
    }

    public Optional<Member> findByProviderId(String providerId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(member).where(member.providerId.eq(providerId)).fetchOne());
    }

    public List<Member> findMemberByDelete() {
        return jpaQueryFactory.selectFrom(member).where(member.deleteState.eq(true)).fetch();
    }

    private BooleanExpression providerIdEq(String providerId) {
        return StringUtils.hasText(providerId) ? member.providerId.eq(providerId) : null;
    }

    private BooleanExpression providerTypeEq(LoginProviderType providerType) {
        return providerType != null ? member.providerType.eq(providerType) : null;
    }
}
