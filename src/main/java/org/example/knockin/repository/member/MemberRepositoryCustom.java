package org.example.knockin.repository.member;

import java.util.List;
import java.util.Optional;

import org.example.knockin.entity.auth.LoginProviderType;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.auth.dto.AuthResponse;

public interface MemberRepositoryCustom {
    Optional<Member> findMemberByProvider(String providerId, LoginProviderType providerType);
    Optional<AuthResponse> findMemberInfo(Member member);
    Optional<Member> findByProviderId(String providerId);
    List<Member> findMemberByDelete();
}
