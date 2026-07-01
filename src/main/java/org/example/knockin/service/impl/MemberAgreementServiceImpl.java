package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.agreement.AgreementLog;
import org.example.knockin.entity.agreement.MemberAgreement;
import org.example.knockin.entity.member.Member;
import org.example.knockin.repository.agreement.MemberAgreementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberAgreementServiceImpl {
    private final MemberAgreementRepository memberAgreementRepository;

    @Transactional
    public List<MemberAgreement> saveAll(List<MemberAgreement> memberAgreementList) {
        return memberAgreementRepository.saveAll(memberAgreementList);
    }

    @Transactional
    public MemberAgreement save(MemberAgreement memberAgreement) {
        return memberAgreementRepository.save(memberAgreement);
    }

    public List<MemberAgreement> findByMember(Member member) {
        return memberAgreementRepository.findByMember(member);
    }

    public List<MemberAgreement> findByMemberAndAgreementLogNotIn(Member member, List<AgreementLog> skipList) {
        return memberAgreementRepository.findByMemberAndAgreementLogNotIn(member, skipList);
    }
}
