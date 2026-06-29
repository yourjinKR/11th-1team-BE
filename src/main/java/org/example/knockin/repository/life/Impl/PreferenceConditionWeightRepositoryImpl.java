package org.example.knockin.repository.life.Impl;

import static org.example.knockin.entity.life.QLifePattern.lifePattern;
import static org.example.knockin.entity.life.QPreferenceConditionWeight.preferenceConditionWeight;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardDetailDto.Response.ConditionWeight;
import org.example.knockin.repository.life.PreferenceConditionWeightRepositoryCustom;
import org.example.knockin.repository.life.row.MatchingPreferenceConditionWeightRow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PreferenceConditionWeightRepositoryImpl implements PreferenceConditionWeightRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ConditionWeight> getConditionWeightDtoByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        ConditionWeight.class,
                        preferenceConditionWeight.id,
                        lifePattern.name
                ))
                .from(preferenceConditionWeight)
                .where(preferenceConditionWeight.member.id.eq(memberId))
                .join(preferenceConditionWeight.lifePattern, lifePattern)
                .fetch();
    }

    @Override
    public List<MatchingPreferenceConditionWeightRow> findAllPreferenceConditionWeightByMemberIdIn(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        MatchingPreferenceConditionWeightRow.class,
                        preferenceConditionWeight.member.id,
                        preferenceConditionWeight.id,
                        lifePattern.id,
                        lifePattern.name
                ))
                .from(preferenceConditionWeight)
                .join(preferenceConditionWeight.lifePattern, lifePattern)
                .where(preferenceConditionWeight.member.id.in(memberIds))
                .orderBy(lifePattern.sort.asc(), preferenceConditionWeight.id.asc())
                .fetch();
    }
}
