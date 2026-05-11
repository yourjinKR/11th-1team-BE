package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.auth.LoginMemberReader;
import org.example.knockin.controller.member.LoginOnboardingRequest;
import org.example.knockin.controller.member.LoginOnboardingRequest.Agreement;
import org.example.knockin.controller.member.LoginOnboardingResponse;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl {
    private final MemberRepository memberRepository;
    private final LoginMemberReader loginMemberReader;

    @Transactional
    public MemberEntity save(String name) {
        return memberRepository.save(MemberEntity.builder().name(name).build());
    }

    public List<MemberEntity> list() {
        String searchName = null;
        return memberRepository.searchMembers(searchName);
    }

    @Transactional
    public LoginOnboardingResponse completeOnboarding(LoginOnboardingRequest request) {

        String name = request.name();
        // TODO: ERD 및 API 명세 확정 후 구체화
        List<Agreement> agreements = request.agreements();
        MemberEntity member = loginMemberReader.getCurrentMember();

        member.completeOnboarding(name);
        return LoginOnboardingResponse.completedOf(member);
    }
}
