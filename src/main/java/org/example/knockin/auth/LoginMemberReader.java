package org.example.knockin.auth;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.repository.MemberRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginMemberReader {

    private final MemberRepository memberRepository;
    private final AuthenticationFacade authenticationFacade;

    public MemberEntity getCurrentMember() {
        AuthMember authenticatedMember = authenticationFacade.getCurrentMember();
        return memberRepository.findById(authenticatedMember.memberId())
                .orElseThrow(() -> new IllegalArgumentException("임시 예외"));
    }
}
