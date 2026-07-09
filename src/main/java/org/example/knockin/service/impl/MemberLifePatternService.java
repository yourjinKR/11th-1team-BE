package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.BoardDetailDto.Response.Lifestyle;
import org.example.knockin.entity.life.MemberLifePattern;
import org.example.knockin.entity.life.MemberLifePatternLog;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.life.MemberLifePatternLogRepository;
import org.example.knockin.repository.life.MemberLifePatternRepository;
import org.example.knockin.repository.life.row.MatchingLifestyleRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberLifePatternService {
    private final MemberLifePatternRepository memberLifePatternRepository;
    private final MemberLifePatternLogRepository memberLifePatternLogRepository;


    @Transactional
    public List<MemberLifePatternLog> saveMemberLifePatternLogAll(List<MemberLifePatternLog> memberLifePatternLogList) {
        return memberLifePatternLogRepository.saveAll(memberLifePatternLogList);
    }

    @Transactional
    public List<MemberLifePattern> saveMemberLifePatternAll(List<MemberLifePattern> memberLifePatternList) {
        return memberLifePatternRepository.saveAll(memberLifePatternList);
    }

    public List<MemberLifePattern> findByMember(Member member) {
        return memberLifePatternRepository.findByMember(member);
    }

    public List<MatchingLifestyleRow> findMatchingRowByMemberIdsIn(List<Long> memberIds) {
        return memberLifePatternRepository.findAllLifestyleByMemberIdIn(memberIds);
    }

    public List<Lifestyle> findLifeStyleDtoByMemberId(Long memberId) {
        return memberLifePatternRepository.getLifeStyleDto(memberId);
    }
}
