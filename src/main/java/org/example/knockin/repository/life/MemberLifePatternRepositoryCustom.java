package org.example.knockin.repository.life;

import java.util.List;
import org.example.knockin.dto.BoardDetailDto;

public interface MemberLifePatternRepositoryCustom {
    List<BoardDetailDto.Response.Lifestyle> getLifeStyleDto(Long memberId);
}