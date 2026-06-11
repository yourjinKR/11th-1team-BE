package org.example.knockin.repository.life.Impl;

import static org.example.knockin.entity.life.QLifePattern.lifePattern;
import static org.example.knockin.entity.life.QPreferenceConditionWeight.preferenceConditionWeight;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardDetailDto.Response.ConditionWeight;
import org.example.knockin.repository.life.PreferenceConditionWeightRepositoryCustom;
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
}
