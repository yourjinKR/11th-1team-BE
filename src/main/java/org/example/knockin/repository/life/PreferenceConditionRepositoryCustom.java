package org.example.knockin.repository.life;


import java.util.List;
import org.example.knockin.dto.BoardDetailDto;

public interface PreferenceConditionRepositoryCustom {
    List<BoardDetailDto.Response.Condition> getConditionDtoByMemberId(Long memberId);
}
