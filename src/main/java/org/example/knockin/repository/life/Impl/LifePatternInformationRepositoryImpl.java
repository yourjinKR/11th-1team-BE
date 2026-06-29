package org.example.knockin.repository.life.Impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.repository.life.LifePatternInformationRepositoryCustom;
import org.example.knockin.repository.life.row.LifePatternInformationValueRow;
import org.springframework.stereotype.Repository;

import java.util.List;
import static org.example.knockin.entity.life.QLifePatternInformation.lifePatternInformation;

@Repository
@RequiredArgsConstructor
public class LifePatternInformationRepositoryImpl implements LifePatternInformationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<LifePatternInformation> findByLifeStyles(List<Long> lifeStyles) {
        return jpaQueryFactory.selectFrom(lifePatternInformation).where(lifePatternInformation.id.in(lifeStyles)).fetch();
    }

    @Override
    public List<LifePatternInformationValueRow> findAllValueRowsByLifePatternIdIn(List<Long> lifePatternIds) {
        if (lifePatternIds == null || lifePatternIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        LifePatternInformationValueRow.class,
                        lifePatternInformation.lifePattern.id,
                        lifePatternInformation.dvalue
                ))
                .from(lifePatternInformation)
                .where(lifePatternInformation.lifePattern.id.in(lifePatternIds))
                .fetch();
    }
}
