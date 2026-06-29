package org.example.knockin.repository.life.row;

import org.example.knockin.global.util.HasMemberId;

public record MatchingPreferenceConditionWeightRow(
        Long memberId,
        Long conditionWeightId,
        Long lifePatternId,
        String name
) implements HasMemberId {
}