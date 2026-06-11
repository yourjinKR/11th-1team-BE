package org.example.knockin.repository.life;

import java.util.List;
import org.example.knockin.dto.BoardDetailDto.Response.ConditionWeight;

public interface PreferenceConditionWeightRepositoryCustom {
    List<ConditionWeight> getConditionWeightDtoByMemberId(Long memberId);
}
