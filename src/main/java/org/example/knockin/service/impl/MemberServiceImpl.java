package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.dto.DeleteUserDto;
import org.example.knockin.entity.member.Member;
import org.example.knockin.entity.member.MemberRole;
import org.example.knockin.global.auth.dto.AuthResponse;
import org.example.knockin.global.auth.dto.OAuth2UserInfo;
import org.example.knockin.global.auth.exception.AuthErrorCode;
import org.example.knockin.global.auth.service.Oauth2DeleteFactory;
import org.example.knockin.global.exception.BusinessException;
import org.example.knockin.repository.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl {
    private final MemberRepository memberRepository;
    private final Oauth2DeleteFactory oauth2DeleteFactory;

    @Transactional
    public Member getOrSave(OAuth2UserInfo oAuth2UserInfo) {
        String providerId = String.valueOf(oAuth2UserInfo.getId());
        return memberRepository.findMemberByProvider(providerId, oAuth2UserInfo.getProviderType())
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .providerType(oAuth2UserInfo.getProviderType())
                            .providerId(String.valueOf(oAuth2UserInfo.getId()))
                            .role(MemberRole.USER)
                            .isDelete(false)
                            .build();
                    return memberRepository.save(newMember);
                });
    }

    public AuthResponse findMemberForLogin(Member member, String accessToken) {
        AuthResponse authResponse = memberRepository.findMemberInfo(member).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));
        authResponse.setAccessToken(accessToken);
        return authResponse;
    }

    @Transactional
    public DeleteUserDto.Response deleteMember(String userName) {
        Member member = memberRepository.findByProviderId(userName).orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND));

        if(oauth2DeleteFactory.getDeleteService(member.getProviderType()).requestUnlink(member.getProviderId())) {
            member.delete();
        } else {
            throw new BusinessException(AuthErrorCode.OAUTH_UNLINK_FAIL);
        }

        return DeleteUserDto.Response.builder().updatedAt(LocalDateTime.now()).build();
    }

    @Transactional
    public void hardDeleteMember() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(5);

        List<Member> memberList = memberRepository.findMemberByDelete();
        List<Member> membersToDelete = memberList.stream().filter(item -> item.getDeletedAt() != null && item.getDeletedAt().isBefore(thresholdDate)).toList();

        if (!membersToDelete.isEmpty()) {
            memberRepository.deleteAll(membersToDelete);
        }
    }
}
