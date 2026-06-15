package org.example.knockin.repository.life.Impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.MetaLifestylePatternsDto;
import org.example.knockin.repository.life.LifePatternRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static org.example.knockin.entity.life.QLifePattern.lifePattern;
import static org.example.knockin.entity.life.QLifePatternInformation.lifePatternInformation;

@Repository
@RequiredArgsConstructor
public class LifePatternRepositoryImpl implements LifePatternRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MetaLifestylePatternsDto.Response.PatternItem> findLifeStylePatterns() {
        return jpaQueryFactory
                .from(lifePattern)
                .leftJoin(lifePatternInformation).on(lifePatternInformation.lifePattern.id.eq(lifePattern.id))
                .where(lifePattern.isDeleted.eq(false))
                .orderBy(lifePattern.sort.asc())
                .transform(groupBy(lifePattern.id).list(Projections.fields(MetaLifestylePatternsDto.Response.PatternItem.class,
                                        lifePattern.id,
                                        lifePattern.name,
                                        lifePattern.dtype.as("type"),
                                        list(Projections.fields(MetaLifestylePatternsDto.Response.PatternItem.DetailItem.class,
                                                lifePatternInformation.dvalue.as("values"),
                                                lifePatternInformation.description
                                        )).as("details"))));
    }
}