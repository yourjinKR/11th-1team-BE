package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.auth.provider.JwtTokenProvider;
import org.example.knockin.auth.provider.JwtTokenProvider.IssuedAccessToken;
import org.example.knockin.auth.provider.SocialOAuthClient;
import org.example.knockin.auth.provider.SocialOAuthClientResolver;
import org.example.knockin.auth.provider.SocialUserInfo;
import org.example.knockin.controller.auth.LoginResponse;
import org.example.knockin.controller.auth.OnBoardingNextStep;
import org.example.knockin.entity.MemberEntity;
import org.example.knockin.entity.member.LoginProvider;
import org.example.knockin.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final MemberRepository memberRepository;
    private final SocialOAuthClientResolver socialOAuthClientResolver;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse loginWithKakao(String providerAccessToken) {
        return login(LoginProvider.KAKAO, providerAccessToken);
    }

    @Transactional
    public LoginResponse login(LoginProvider provider, String providerToken) {
        SocialOAuthClient socialOAuthClient = socialOAuthClientResolver.resolve(provider);
        SocialUserInfo userInfo = socialOAuthClient.getUserInfo(providerToken);
        return loginWithSocialUser(userInfo);
    }

    private LoginResponse loginWithSocialUser(SocialUserInfo userInfo) {
        MemberEntity member = memberRepository.findByProviderAndProviderId(userInfo.provider(), userInfo.providerId())
                .orElseGet(() -> {
                    MemberEntity newMember = MemberEntity.pendingMember(
                            userInfo.provider(),
                            userInfo.providerId()
                    );
                    return memberRepository.save(newMember);
                });

        OnBoardingNextStep nextStep = OnBoardingNextStep.from(member.getStatus());
        IssuedAccessToken issuedAccessToken = jwtTokenProvider.createToken(member);

        return LoginResponse.of(
                issuedAccessToken.raw(),
                "Bearer",
                issuedAccessToken.expiresIn(),
                member,
                nextStep
        );
    }
}
