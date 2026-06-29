package org.example.knockin.repository.life.row;

import org.example.knockin.entity.life.LifePatternType;
import org.example.knockin.global.util.HasMemberId;

public record MatchingPreferenceConditionRow(
        Long memberId,
        Long conditionId,
        Long lifePatternId,
        Long lifePatternInformationId,
        String name,
        String value,
        String description,
        LifePatternType type
) implements HasMemberId {
}