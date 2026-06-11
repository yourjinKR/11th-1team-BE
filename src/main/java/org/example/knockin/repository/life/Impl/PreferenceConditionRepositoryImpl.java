package org.example.knockin.repository.life.Impl;

import static org.example.knockin.entity.life.QLifePattern.lifePattern;
import static org.example.knockin.entity.life.QLifePatternInformation.lifePatternInformation;
import static org.example.knockin.entity.life.QPreferenceCondition.preferenceCondition;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.repository.life.PreferenceConditionRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PreferenceConditionRepositoryImpl implements PreferenceConditionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<BoardDetailDto.Response.Condition> getConditionDtoByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardDetailDto.Response.Condition.class,
                        preferenceCondition.id,
                        lifePattern.name,
                        lifePatternInformation.dvalue,
                        lifePatternInformation.description,
                        lifePattern.dtype
                ))
                .from(preferenceCondition)
                .where(preferenceCondition.member.id.eq(memberId))
                .join(preferenceCondition.lifePatternInformation, lifePatternInformation)
                .leftJoin(lifePatternInformation.lifePattern, lifePattern)
                .fetch();
    }
}
