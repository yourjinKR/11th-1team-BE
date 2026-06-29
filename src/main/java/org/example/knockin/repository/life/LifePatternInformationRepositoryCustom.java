package org.example.knockin.repository.life;

import org.example.knockin.entity.life.LifePatternInformation;
import org.example.knockin.repository.life.row.LifePatternInformationValueRow;

import java.util.List;

public interface LifePatternInformationRepositoryCustom {
    List<LifePatternInformation> findByLifeStyles(List<Long> lifeStyles);

    List<LifePatternInformationValueRow> findAllValueRowsByLifePatternIdIn(List<Long> lifePatternIds);
}