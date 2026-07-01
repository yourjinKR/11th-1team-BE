package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberPrivacy;
import org.example.knockin.repository.member.MemberPrivacyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberPrivacyServiceImpl {
    private final MemberPrivacyRepository memberPrivacyRepository;

    public List<MemberPrivacy> findByMember(Member member) {
        return memberPrivacyRepository.findByMember(member);
    }

    public List<MemberPrivacy> findByMemberId(Long memberId) {
        return memberPrivacyRepository.findByMemberId(memberId);
    }

    @Transactional
    public MemberPrivacy save(MemberPrivacy memberPrivacy) {
        return memberPrivacyRepository.save(memberPrivacy);
    }
}
