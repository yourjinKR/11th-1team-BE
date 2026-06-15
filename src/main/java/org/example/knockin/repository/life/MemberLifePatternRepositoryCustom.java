package org.example.knockin.repository.life;

import java.util.List;
import org.example.knockin.dto.BoardDetailDto;
import org.example.knockin.entity.member.Member;

public interface MemberLifePatternRepositoryCustom {
    List<BoardDetailDto.Response.Lifestyle> getLifeStyleDto(Long memberId);
    boolean isExsitLifeStyle(Member member);
}