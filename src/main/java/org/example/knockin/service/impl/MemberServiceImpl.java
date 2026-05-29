package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.repository.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl {
    private final MemberRepository memberRepository;

    @Transactional
    public Member save(String name) {
        return getDummyMember();
    }

    public List<Member> list() {
        return List.of(getDummyMember());
    }

    public Member getDummyMember() {
        return new Member(1L, LoginProviderType.KAKAO, "akdahdadha", MemberRole.USER);
    }
}
